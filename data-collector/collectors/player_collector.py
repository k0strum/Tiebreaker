import time
import random
import json
import requests
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait, Select
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException
from utils.config import get_config

PLAYER_INFO_MAP = {
    '생년월일': 'birthday',
    '신장/체중': 'heightWeight',
    '지명순위': 'draftRank',
    '등번호': 'backNumber',
    '포지션': 'position',
    '경력': 'career'
}

# 팀 코드와 팀명 매핑
TEAM_CODE_TO_NAME = {
    'LG': 'LG',
    'OB': 'DOOSAN',
    'HH': 'HANHWA',
    'HT': 'KIA',
    'SS': 'SAMSUNG',
    'KT': 'KT',
    'SK': 'SSG',
    'LT': 'LOTTE',
    'NC': 'NC',
    'WO': 'KIWOOM'
}

# 타자 순수 스탯 (DTO와 매칭)
HITTER_BASE_STATS = {
    # 상단 섹션 - 기본 기록
    'G': 'games',                    # 경기 수
    'PA': 'plateAppearances',        # 타석
    'AB': 'atBats',                  # 타수
    'R': 'runs',                     # 득점
    'H': 'hits',                     # 안타
    '2B': 'doubles',                 # 2루타
    '3B': 'triples',                 # 3루타
    'HR': 'homeRuns',                # 홈런
    'TB': 'totalBases',              # 총 루타
    'RBI': 'runsBattedIn',           # 타점
    'SB': 'stolenBases',             # 도루
    'CS': 'caughtStealing',          # 도루실패
    'SAC': 'sacrificeHits',          # 희생번트
    'SF': 'sacrificeFlies',          # 희생플라이
    
    # 하단 섹션 - 볼넷, 삼진, 실책
    'BB': 'walks',                   # 볼넷
    'IBB': 'intentionalWalks',       # 고의사구
    'HBP': 'hitByPitch',             # 사구
    'SO': 'strikeouts',              # 삼진
    'GDP': 'groundedIntoDoublePlay', # 병살타
    'E': 'errors'                    # 실책
}

# 타자 계산 지표 (검증용)
HITTER_CALCULATED_STATS = {
    'AVG': 'battingAverage',         # 타율
    'SLG': 'sluggingPercentage',     # 장타율
    'OBP': 'onBasePercentage',       # 출루율
    'SB%': 'stolenBasePercentage',   # 도루 성공률
    'OPS': 'ops',                    # OPS
    'RISP': 'battingAverageWithRunnersInScoringPosition',  # 득점권 타율
    'PH-BA': 'pinchHitBattingAverage'  # 대타 타율
}

# 투수 순수 스탯 (DTO와 매칭)
PITCHER_BASE_STATS = {
    # 상단 섹션 - 승패 기록
    'G': 'games',                    # 경기 수
    'W': 'wins',                     # 승
    'L': 'losses',                   # 패
    'SV': 'saves',                   # 세이브
    'HLD': 'holds',                  # 홀드
    'BSV': 'blownSaves',             # 블론세이브
    
    # 완투, 완봉
    'CG': 'completeGames',           # 완투
    'SHO': 'shutouts',               # 완봉
    
    # 투구 관련
    'TBF': 'totalBattersFaced',      # 상대 타자 수
    'NP': 'numberOfPitches',         # 투구 수
    'IP': 'inningsPitched',          # 이닝
    
    # 허용 기록
    'H': 'hitsAllowed',              # 허용 안타
    '2B': 'doublesAllowed',          # 허용 2루타
    '3B': 'triplesAllowed',          # 허용 3루타
    'HR': 'homeRunsAllowed',         # 허용 홈런
    'SAC': 'sacrificeHitsAllowed',   # 허용 희생번트
    'SF': 'sacrificeFliesAllowed',   # 허용 희생플라이
    'BB': 'walksAllowed',            # 허용 볼넷
    'IBB': 'intentionalWalksAllowed', # 허용 고의사구
    'R': 'runsAllowed',              # 허용 실점
    'ER': 'earnedRuns',              # 자책점
    'SO': 'strikeouts',              # 삼진
    
    # 기타
    'WP': 'wildPitches',             # 폭투
    'BK': 'balks',                   # 보크
    'QS': 'qualityStarts'            # 퀄리티스타트
}

# 투수 계산 지표 (검증용)
PITCHER_CALCULATED_STATS = {
    'ERA': 'earnedRunAverage',       # 평균자책점
    'WPCT': 'winningPercentage',     # 승률
    'WHIP': 'whip',                  # WHIP
    'AVG': 'battingAverageAgainst'   # 피안타율
}

def scrape_all_players_and_details():
    """
    모든 선수의 상세 정보를 수집하는 제너레이터 함수
    
    Yields:
        dict: 선수 정보 딕셔너리
    """
    print("[DEBUG] scrape_all_players_and_details 함수에 진입했습니다.")
    config = get_config()
    options = webdriver.ChromeOptions()

    # --- 옵션 설정 ---
    if config['headless']:
        options.add_argument('--headless')
    options.add_argument('--no-sandbox')
    options.add_argument('--disable-dev-shm-usage')
    options.add_argument('--window-size=1920,1080')
    options.add_argument('user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36')
    options.add_argument('--disable-blink-features=AutomationControlled')
    options.add_argument('--disable-extensions')
    options.add_argument('--disable-plugins')

    print("[DEBUG] WebDriver 옵션 설정을 완료했습니다. 이제 드라이버 초기화를 시도합니다...")

    driver = None
    try:
        print("[DEBUG] WebDriver 옵션 설정을 완료했습니다. 이제 드라이버 초기화를 시도합니다...")
        driver = webdriver.Chrome(options=options)
        driver.set_page_load_timeout(30)
        print("[DEBUG] WebDriver 초기화에 성공했습니다!")
    except Exception as e:
        print(f"[CRITICAL] WebDriver를 초기화하는 중 심각한 오류가 발생했습니다: {e}")
        return

    try:
        # team_codes = ['LG', 'OB', 'HH', 'HT', 'SS', 'KT', 'SK', 'LT', 'NC', 'WO']
        team_codes = ['LT']
        search_url = "https://www.koreabaseball.com/Player/Search.aspx"
        wait = WebDriverWait(driver, 20)
        
        for team_code in team_codes:
            # 팀 코드로 팀명 가져오기
            team_name = TEAM_CODE_TO_NAME.get(team_code, f"팀코드_{team_code}")
            print(f"\n{team_code} 팀({team_name}) 목록 수집 시작...")
            current_page_num = 1
            while True:
                # --- [로직 수정] ---
                # 바깥 루프에서는 더 이상 페이지를 로드하지 않고,
                # 안쪽 루프가 모든 것을 처리하도록 구조를 단순화합니다.
                # 먼저 해당 페이지에 선수가 몇 명 있는지 확인합니다.
                
                driver.get(search_url)
                Select(wait.until(EC.presence_of_element_located((By.ID, 'cphContents_cphContents_cphContents_ddlTeam')))).select_by_value(team_code)
                
                # 페이지 이동이 필요한 경우
                if current_page_num > 1:
                    try:
                        # 이동하려는 페이지 링크가 보이는지 먼저 확인
                        page_link_to_find = wait.until(EC.presence_of_element_located((By.LINK_TEXT, str(current_page_num))))
                        first_player_on_old_page = wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, ".tEx tbody tr:first-child > td:nth-child(2) > a")))
                        driver.execute_script("arguments[0].click();", page_link_to_find)
                        wait.until(EC.staleness_of(first_player_on_old_page))
                    except TimeoutException:
                        print(f"    {current_page_num} 페이지를 찾을 수 없어 '{team_code}' 팀 수집을 종료합니다.")
                        break # while 루프 탈출

                # 현재 페이지의 선수 수를 파악
                num_players_on_page = len(wait.until(EC.presence_of_all_elements_located((By.CSS_SELECTOR, ".tEx tbody tr > td:nth-child(2) > a"))))
                if num_players_on_page == 0:
                    break

                print(f"\n--- {team_code} 팀 {current_page_num} 페이지({num_players_on_page}명) 처리 시작 ---")
                
                # 한 페이지 내의 선수들을 순회
                for i in range(num_players_on_page):
                    player_name = ""
                    try:
                        # [핵심] 선수 한 명을 처리하기 위해 페이지를 처음부터 다시 로드하고 이동
                        driver.get(search_url)
                        Select(wait.until(EC.presence_of_element_located((By.ID, 'cphContents_cphContents_cphContents_ddlTeam')))).select_by_value(team_code)

                        if current_page_num > 1:
                            # 이전 페이지의 요소를 먼저 잡고
                            first_player_on_old_page_inner = wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, ".tEx tbody tr:first-child > td:nth-child(2) > a")))
                            # 페이지 링크 클릭
                            page_link_inner = wait.until(EC.element_to_be_clickable((By.LINK_TEXT, str(current_page_num))))
                            driver.execute_script("arguments[0].click();", page_link_inner)
                            # 페이지 갱신 대기 (staleness_of)
                            wait.until(EC.staleness_of(first_player_on_old_page_inner))

                        all_links = wait.until(EC.presence_of_all_elements_located((By.CSS_SELECTOR, ".tEx tbody tr > td:nth-child(2) > a")))
                        
                        if i >= len(all_links):
                            print(f"    [경고] {i+1}번째 선수 링크를 찾을 수 없습니다. 페이지를 건너뜁니다.")
                            break
                        
                        player_to_click = all_links[i]
                        player_name = player_to_click.text.strip()
                        
                        if not player_name:
                            print(f"    [경고] {i+1}번째 선수 이름이 비어있습니다. 건너뜁니다.")
                            continue
                            
                        print(f"  ({i+1}/{num_players_on_page}) {player_name} 선수 정보 수집 시도...")
                        
                        # 선수 링크 클릭 시도
                        try:
                            # 먼저 클릭 가능한 상태인지 확인
                            wait.until(EC.element_to_be_clickable(player_to_click))
                            driver.execute_script("arguments[0].click();", player_to_click)
                        except Exception as click_error:
                            print(f"    [경고] {player_name} 선수 링크 클릭 실패: {click_error}")
                            # 기본 정보만 포함한 데이터 반환
                            player_data = {
                                'id': None, 
                                'playerName': player_name, 
                                'teamName': team_name,
                                'batterStats': None,
                                'batterCalculatedStats': None,
                                'pitcherStats': None,
                                'pitcherCalculatedStats': None
                            }
                            yield player_data
                            continue
                        
                        # 선수 상세 페이지 로딩 대기 (타임아웃 처리 추가)
                        try:
                            wait.until(EC.presence_of_element_located((By.CLASS_NAME, "player_basic")))
                        except TimeoutException:
                            print(f"    [경고] {player_name} 선수 상세 페이지 로딩 타임아웃. 검색 페이지로 돌아갑니다.")
                            # 현재 URL 확인
                            current_url = driver.current_url
                            if "Search.aspx" in current_url:
                                print(f"    [확인] 여전히 검색 페이지에 있습니다: {current_url}")
                                # 기본 정보만 포함한 데이터 반환
                                player_data = {
                                    'id': None, 
                                    'playerName': player_name, 
                                    'teamName': team_name,
                                    'batterStats': None,
                                    'batterCalculatedStats': None,
                                    'pitcherStats': None,
                                    'pitcherCalculatedStats': None
                                }
                                yield player_data
                                continue
                            else:
                                # 다른 페이지로 이동했다면 다시 시도
                                print(f"    [재시도] 다른 페이지로 이동했습니다: {current_url}")
                                try:
                                    wait.until(EC.presence_of_element_located((By.CLASS_NAME, "player_basic")))
                                except TimeoutException:
                                    print(f"    [최종 실패] {player_name} 선수 페이지 로딩 실패")
                                    player_data = {
                                        'id': None, 
                                        'playerName': player_name, 
                                        'teamName': team_name,
                                        'batterStats': None,
                                        'batterCalculatedStats': None,
                                        'pitcherStats': None,
                                        'pitcherCalculatedStats': None
                                    }
                                    yield player_data
                                    continue
                        
                        soup = BeautifulSoup(driver.page_source, 'html.parser')

                        try:
                            # 'player_basic' 클래스 내부의 img 태그를 찾습니다.
                            img_tag = soup.select_one('.player_basic .photo img')
                            if img_tag and img_tag.has_attr('src'):
                                image_url = img_tag['src']
                                # URL이 //로 시작하는 경우 https:를 붙여줍니다.
                                if image_url.startswith('//'):
                                    image_url = 'https:' + image_url
                        except Exception:
                            print(f"    [경고] {player_name} 선수의 이미지 URL을 찾을 수 없습니다.")
                            pass

                        # 1. Selenium으로 현재 페이지의 URL을 직접 가져옵니다.
                        current_url = driver.current_url
                        player_id = None # 기본값 설정

                        if 'playerId=' in current_url:
                            try:
                                # 'playerId='를 기준으로 문자열을 잘라 뒷부분을 숫자로 변환합니다.
                                player_id = int(current_url.split('playerId=')[1])
                            except (IndexError, ValueError):
                                # 만약 자르거나 숫자 변환에 실패하면 ID는 그대로 None이 됩니다.
                                print(f"  [경고] {player_name} 선수의 URL에서 ID를 파싱할 수 없습니다.")
                                pass

                        player_data = {'id': player_id, 'playerName': player_name, 'teamName': team_name}

                        info_list_items = soup.select('.player_info li')
                        for item in info_list_items:
                            key_tag = item.find('strong')
                            if key_tag:
                                korean_key = key_tag.text.strip().replace(':', '')
                                if korean_key in PLAYER_INFO_MAP:
                                    english_key = PLAYER_INFO_MAP[korean_key]
                                    value = item.text.replace(key_tag.text, '').strip()
                                    player_data[english_key] = value

                        combined_raw_stats = {}

                        # 1. 선수 기록 영역 안의 모든 기록 테이블을 찾습니다. (3개가 찾아짐)
                        all_stats_tables = soup.select('.player_records table.tbl.tt')

                        if not all_stats_tables or "기록이 없습니다" in all_stats_tables[0].text:
                            print(f"    [정보] {player_name} 선수는 파싱할 스탯 테이블이 없습니다.")
                            player_data['batterStats'] = None
                            player_data['batterCalculatedStats'] = None
                            player_data['pitcherStats'] = None
                            player_data['pitcherCalculatedStats'] = None
                            yield player_data
                            continue # 다음 선수로 넘어감

                        # 2. 찾은 테이블 중 앞의 두 개만 선택합니다. (최근 10경기 제외)
                        tables_to_parse = all_stats_tables[:2]

                        # 3. 선택된 두 개의 테이블을 순회하며 데이터 추출 및 병합합니다.
                        for table in tables_to_parse:
                            headers = [th.text.strip() for th in table.select('thead th')]
                            values = [td.text.strip() for td in table.select('tbody td')]
                            
                            temp_stats = dict(zip(headers, values))
                            combined_raw_stats.update(temp_stats)

                        # 순수 스탯과 계산 지표 분리 처리
                        base_stats_dict = {}
                        calculated_stats_dict = {}
                        
                        if combined_raw_stats:
                            # 타자 스탯인지 투수 스탯인지 판별 (RBI가 있으면 타자, ERA가 있으면 투수)
                            is_batter = any('RBI' in header for header in combined_raw_stats.keys())
                            is_pitcher = any('ERA' in header for header in combined_raw_stats.keys())
                            
                            if is_batter:
                                # 타자 스탯 처리
                                for header, value in combined_raw_stats.items():
                                    # 순수 스탯 처리
                                    if header in HITTER_BASE_STATS:
                                        key = HITTER_BASE_STATS[header]
                                        try:
                                            if '.' in value:
                                                base_stats_dict[key] = float(value)
                                            else:
                                                base_stats_dict[key] = int(value)
                                        except (ValueError, TypeError):
                                            base_stats_dict[key] = 0
                                    
                                    # 계산 지표 처리
                                    elif header in HITTER_CALCULATED_STATS:
                                        key = HITTER_CALCULATED_STATS[header]
                                        try:
                                            if '.' in value:
                                                calculated_stats_dict[key] = float(value)
                                            else:
                                                calculated_stats_dict[key] = int(value)
                                        except (ValueError, TypeError):
                                            calculated_stats_dict[key] = 0
                            
                            elif is_pitcher:
                                # 투수 스탯 처리
                                for header, value in combined_raw_stats.items():
                                    # 순수 스탯 처리
                                    if header in PITCHER_BASE_STATS:
                                        key = PITCHER_BASE_STATS[header]
                                        try:
                                            if '.' in value:
                                                base_stats_dict[key] = float(value)
                                            else:
                                                base_stats_dict[key] = int(value)
                                        except (ValueError, TypeError):
                                            base_stats_dict[key] = 0
                                    
                                    # 계산 지표 처리
                                    elif header in PITCHER_CALCULATED_STATS:
                                        key = PITCHER_CALCULATED_STATS[header]
                                        try:
                                            if '.' in value:
                                                calculated_stats_dict[key] = float(value)
                                            else:
                                                calculated_stats_dict[key] = int(value)
                                        except (ValueError, TypeError):
                                            calculated_stats_dict[key] = 0

                        # 선수 타입에 따라 스탯 저장
                        if is_pitcher:
                            player_data['pitcherStats'] = base_stats_dict
                            player_data['pitcherCalculatedStats'] = calculated_stats_dict
                            player_data['batterStats'] = None
                            player_data['batterCalculatedStats'] = None
                        elif is_batter:
                            player_data['batterStats'] = base_stats_dict
                            player_data['batterCalculatedStats'] = calculated_stats_dict
                            player_data['pitcherStats'] = None
                            player_data['pitcherCalculatedStats'] = None
                        else:
                            player_data['batterStats'] = None
                            player_data['batterCalculatedStats'] = None
                            player_data['pitcherStats'] = None
                            player_data['pitcherCalculatedStats'] = None
                        
                        yield player_data
                        
                    except Exception as e:
                        print(f"    [에러] {i+1}번째 선수 ({player_name}) 처리 중 예외 발생: {e}")
                        try:
                            # 1. 실패 당시의 URL 출력
                            current_url = driver.current_url
                            print(f"    [URL] 실패 당시 URL: {current_url}")

                            # 2. 실패 당시의 화면 스크린샷 저장
                            screenshot_path = f"error_screenshot_player_{i+1}_{player_name}.png"
                            driver.save_screenshot(screenshot_path)
                            print(f"    [Screenshot] 에러 화면을 '{screenshot_path}' 파일로 저장했습니다.")
            
                            # 3. 실패 당시의 HTML 소스 저장
                            html_path = f"error_page_player_{i+1}_{player_name}.html"
                            with open(html_path, 'w', encoding='utf-8') as f:
                                f.write(driver.page_source)
                            print(f"    [HTML] 에러 페이지 소스를 '{html_path}' 파일로 저장했습니다.")

                        except Exception as debug_e:
                            print(f"    [디버깅 에러] HTML 저장 중 추가 오류 발생: {debug_e}")
                        print(f"    [Original Error] {e.__class__.__name__}")
                        continue
                
                current_page_num += 1
    
    finally:
        if 'driver' in locals() and driver:
            driver.quit()
        print("\n모든 선수 정보 수집 완료.")
