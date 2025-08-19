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
    'OB': '두산',
    'HH': '한화',
    'HT': 'KIA',
    'SS': '삼성',
    'KT': 'KT',
    'SK': 'SSG',
    'LT': '롯데',
    'NC': 'NC',
    'WO': '키움'
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

def create_default_player_data(player_name, team_name):
    """기본 선수 데이터 구조를 생성하는 헬퍼 함수"""
    return {
        'id': None, 
        'playerName': player_name, 
        'teamName': team_name,
        'imageUrl': None,
        'batterStats': None,
        'pitcherStats': None
    }

def navigate_to_team_page(driver, wait, search_url, team_code, page_num=1):
    """팀 페이지로 이동하는 공통 함수"""
    driver.get(search_url)
    Select(wait.until(EC.presence_of_element_located((By.ID, 'cphContents_cphContents_cphContents_ddlTeam')))).select_by_value(team_code)
    
    if page_num > 1:
        try:
            page_link = wait.until(EC.presence_of_element_located((By.LINK_TEXT, str(page_num))))
            first_player = wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, ".tEx tbody tr:first-child > td:nth-child(2) > a")))
            driver.execute_script("arguments[0].click();", page_link)
            wait.until(EC.staleness_of(first_player))
            return True
        except TimeoutException:
            return False
    return True

def get_player_links(driver, wait):
    """현재 페이지의 선수 링크들을 가져오는 함수"""
    return wait.until(EC.presence_of_all_elements_located((By.CSS_SELECTOR, ".tEx tbody tr > td:nth-child(2) > a")))

def extract_player_id_from_url(url):
    """URL에서 선수 ID를 추출하는 함수"""
    if 'playerId=' in url:
        try:
            # playerId= 다음부터 & 또는 끝까지 추출
            player_id_part = url.split('playerId=')[1]
            # &가 있으면 그 앞까지만, 없으면 끝까지
            if '&' in player_id_part:
                player_id_str = player_id_part.split('&')[0]
            else:
                player_id_str = player_id_part
            
            player_id = int(player_id_str)
            print(f"    [정보] 선수 ID 추출 성공: {player_id}")
            return player_id
        except (IndexError, ValueError) as e:
            print(f"    [경고] URL에서 선수 ID 파싱 실패: {url}, 오류: {e}")
            return None
    else:
        print(f"    [경고] URL에 playerId 파라미터가 없습니다: {url}")
        return None

def extract_image_url(soup, player_name):
    """선수 이미지 URL을 추출하는 함수"""
    try:
        img_tag = soup.select_one('.player_basic .photo img')
        if img_tag and img_tag.has_attr('src'):
            image_url = img_tag['src']
            if image_url.startswith('//'):
                image_url = 'https:' + image_url
            print(f"    [정보] {player_name} 선수 이미지 URL 수집: {image_url}")
            return image_url
        else:
            print(f"    [경고] {player_name} 선수의 이미지 태그를 찾을 수 없습니다.")
    except Exception as e:
        print(f"    [경고] {player_name} 선수의 이미지 URL을 찾을 수 없습니다: {e}")
    return None

def extract_player_basic_info(soup):
    """선수 기본 정보를 추출하는 함수"""
    player_info = {}
    info_list_items = soup.select('.player_info li')
    for item in info_list_items:
        key_tag = item.find('strong')
        if key_tag:
            korean_key = key_tag.text.strip().replace(':', '')
            if korean_key in PLAYER_INFO_MAP:
                english_key = PLAYER_INFO_MAP[korean_key]
                value = item.text.replace(key_tag.text, '').strip()
                player_info[english_key] = value
    return player_info

def process_stats_tables(soup, player_name):
    """스탯 테이블을 처리하는 함수"""
    all_stats_tables = soup.select('.player_records table.tbl.tt')
    
    if not all_stats_tables or "기록이 없습니다" in all_stats_tables[0].text:
        print(f"    [정보] {player_name} 선수는 파싱할 스탯 테이블이 없습니다.")
        return None  # None만 반환하도록 수정
    
    # 앞의 두 개 테이블만 선택 (최근 10경기 제외)
    tables_to_parse = all_stats_tables[:2]
    combined_raw_stats = {}
    
    for table in tables_to_parse:
        headers = [th.text.strip() for th in table.select('thead th')]
        values = [td.text.strip() for td in table.select('tbody td')]
        temp_stats = dict(zip(headers, values))
        combined_raw_stats.update(temp_stats)
    
    return combined_raw_stats

def parse_player_stats(combined_raw_stats):
    """선수 스탯을 파싱하는 함수 - 엔티티 구조에 맞춰 하나의 딕셔너리로 합침"""
    stats_dict = {}
    
    if not combined_raw_stats:
        return None, None, None, None
    
    # 타자/투수 판별
    is_batter = any('RBI' in header for header in combined_raw_stats.keys())
    is_pitcher = any('ERA' in header for header in combined_raw_stats.keys())
    
    if is_batter:
        for header, value in combined_raw_stats.items():
            # 기본 스탯 처리
            if header in HITTER_BASE_STATS:
                key = HITTER_BASE_STATS[header]
                try:
                    stats_dict[key] = float(value) if '.' in value else int(value)
                except (ValueError, TypeError):
                    stats_dict[key] = 0
            # 계산 지표 처리
            elif header in HITTER_CALCULATED_STATS:
                key = HITTER_CALCULATED_STATS[header]
                try:
                    stats_dict[key] = float(value) if '.' in value else int(value)
                except (ValueError, TypeError):
                    stats_dict[key] = 0
    
    elif is_pitcher:
        for header, value in combined_raw_stats.items():
            # 기본 스탯 처리
            if header in PITCHER_BASE_STATS:
                key = PITCHER_BASE_STATS[header]
                try:
                    if key == 'inningsPitched':
                        stats_dict[key] = value
                    else:
                        stats_dict[key] = float(value) if '.' in value else int(value)
                except (ValueError, TypeError):
                    stats_dict[key] = 0
            # 계산 지표 처리
            elif header in PITCHER_CALCULATED_STATS:
                key = PITCHER_CALCULATED_STATS[header]
                try:
                    stats_dict[key] = float(value) if '.' in value else int(value)
                except (ValueError, TypeError):
                    stats_dict[key] = 0
    
    return stats_dict, None, is_batter, is_pitcher

def save_error_debug_info(driver, i, player_name):
    """에러 디버깅 정보를 저장하는 함수"""
    try:
        current_url = driver.current_url
        print(f"    [URL] 실패 당시 URL: {current_url}")
        
        screenshot_path = f"error_screenshot_player_{i+1}_{player_name}.png"
        driver.save_screenshot(screenshot_path)
        print(f"    [Screenshot] 에러 화면을 '{screenshot_path}' 파일로 저장했습니다.")
        
        html_path = f"error_page_player_{i+1}_{player_name}.html"
        with open(html_path, 'w', encoding='utf-8') as f:
            f.write(driver.page_source)
        print(f"    [HTML] 에러 페이지 소스를 '{html_path}' 파일로 저장했습니다.")
    except Exception as debug_e:
        print(f"    [디버깅 에러] HTML 저장 중 추가 오류 발생: {debug_e}")

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
    if config['webdriver']['headless']:
        options.add_argument('--headless')
    options.add_argument('--no-sandbox')
    options.add_argument('--disable-dev-shm-usage')
    options.add_argument('--window-size=1920,1080')
    options.add_argument('user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36')
    options.add_argument('--disable-blink-features=AutomationControlled')
    options.add_argument('--disable-extensions')
    options.add_argument('--disable-plugins')
    
    # 로그 레벨 조정 (불필요한 메시지 숨기기)
    options.add_argument('--log-level=3')  # 오류만 표시
    options.add_argument('--silent')
    options.add_experimental_option('excludeSwitches', ['enable-logging'])

    print("[DEBUG] WebDriver 옵션 설정을 완료했습니다. 이제 드라이버 초기화를 시도합니다...")

    driver = None
    try:
        driver = webdriver.Chrome(options=options)
        driver.set_page_load_timeout(30)
        print("[DEBUG] WebDriver 초기화에 성공했습니다!")
    except Exception as e:
        print(f"[CRITICAL] WebDriver를 초기화하는 중 심각한 오류가 발생했습니다: {e}")
        return

    try:
        # team_codes = ['LG', 'OB', 'HH', 'HT', 'SS', 'KT', 'SK', 'LT', 'NC', 'WO']
        team_codes = ['SK', 'WO']
        search_url = "https://www.koreabaseball.com/Player/Search.aspx"
        wait = WebDriverWait(driver, 25)  # 대기 시간 증가 (20 -> 25초)
        
        for team_code in team_codes:
            team_name = TEAM_CODE_TO_NAME.get(team_code, f"팀코드_{team_code}")
            print(f"\n{team_code} 팀({team_name}) 목록 수집 시작...")
            current_page_num = 1
            
            while True:
                # 페이지 이동 시도
                if not navigate_to_team_page(driver, wait, search_url, team_code, current_page_num):
                    print(f"    {current_page_num} 페이지를 찾을 수 없어 '{team_code}' 팀 수집을 종료합니다.")
                    break

                # 현재 페이지의 선수 수를 파악
                all_links = get_player_links(driver, wait)
                num_players_on_page = len(all_links)
                if num_players_on_page == 0:
                    break

                print(f"\n--- {team_code} 팀 {current_page_num} 페이지({num_players_on_page}명) 처리 시작 ---")
                
                # 한 페이지 내의 선수들을 순회
                for i in range(num_players_on_page):
                    player_name = ""
                    try:
                        # 선수 한 명을 처리하기 위해 페이지를 처음부터 다시 로드하고 이동
                        if not navigate_to_team_page(driver, wait, search_url, team_code, current_page_num):
                            print(f"    [경고] {current_page_num} 페이지 이동 실패. 다음 선수로 넘어갑니다.")
                            continue

                        all_links = get_player_links(driver, wait)
                        
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
                            wait.until(EC.element_to_be_clickable(player_to_click))
                            driver.execute_script("arguments[0].click();", player_to_click)
                            # 클릭 후 랜덤 대기 (페이지 로딩 안정화)
                            time.sleep(random.uniform(1.5, 2.5))
                        except Exception as click_error:
                            print(f"    [경고] {player_name} 선수 링크 클릭 실패: {click_error}")
                            yield create_default_player_data(player_name, team_name)
                            continue
                        
                        # 선수 상세 페이지 로딩 대기
                        try:
                            wait.until(EC.presence_of_element_located((By.CLASS_NAME, "player_basic")))
                            # 추가 랜덤 대기 시간 (페이지 완전 로딩)
                            time.sleep(random.uniform(0.8, 1.5))
                        except TimeoutException:
                            print(f"    [경고] {player_name} 선수 상세 페이지 로딩 타임아웃.")
                            current_url = driver.current_url
                            if "Search.aspx" in current_url:
                                print(f"    [확인] 여전히 검색 페이지에 있습니다: {current_url}")
                                yield create_default_player_data(player_name, team_name)
                                continue
                            else:
                                print(f"    [재시도] 다른 페이지로 이동했습니다: {current_url}")
                                try:
                                    wait.until(EC.presence_of_element_located((By.CLASS_NAME, "player_basic")))
                                    time.sleep(random.uniform(0.8, 1.5))
                                except TimeoutException:
                                    print(f"    [최종 실패] {player_name} 선수 페이지 로딩 실패")
                                    yield create_default_player_data(player_name, team_name)
                                    continue
                        
                        # 현재 URL 확인 - 퓨처스리그 페이지인지 체크
                        current_url = driver.current_url
                        if "Futures" in current_url:
                            print(f"    [퓨처스리그] {player_name} 선수는 퓨처스리그 페이지로 리다이렉트되었습니다. 기본 정보만 수집합니다.")
                            soup = BeautifulSoup(driver.page_source, 'html.parser')
                            
                            # 이미지 URL 수집
                            image_url = extract_image_url(soup, player_name)
                            
                            # 선수 ID 추출
                            player_id = extract_player_id_from_url(current_url)
                            
                            # ID 검증 - ID가 없으면 건너뛰기
                            if player_id is None:
                                print(f"    [경고] {player_name} 선수의 ID를 추출할 수 없어 건너뜁니다.")
                                continue
                            
                            # 기본 선수 데이터 생성
                            player_data = {
                                'id': player_id, 
                                'playerName': player_name, 
                                'teamName': team_name,
                                'imageUrl': image_url
                            }
                            
                            # 기본 정보 추가
                            basic_info = extract_player_basic_info(soup)
                            player_data.update(basic_info)
                            
                            # 퓨처스리그 선수는 스탯 정보 없음
                            player_data['batterStats'] = None
                            player_data['pitcherStats'] = None
                            
                            yield player_data
                            continue
                        
                        soup = BeautifulSoup(driver.page_source, 'html.parser')

                        # 이미지 URL 수집
                        image_url = extract_image_url(soup, player_name)

                        # 선수 ID 추출
                        player_id = extract_player_id_from_url(current_url)
                        
                        # ID 검증 - ID가 없으면 건너뛰기
                        if player_id is None:
                            print(f"    [경고] {player_name} 선수의 ID를 추출할 수 없어 건너뜁니다.")
                            continue

                        # 기본 선수 데이터 생성
                        player_data = {
                            'id': player_id, 
                            'playerName': player_name, 
                            'teamName': team_name,
                            'imageUrl': image_url
                        }

                        # 기본 정보 추가
                        basic_info = extract_player_basic_info(soup)
                        player_data.update(basic_info)

                        # 스탯 정보 처리
                        combined_raw_stats = process_stats_tables(soup, player_name)
                        if combined_raw_stats is None:
                            # 스탯이 없는 경우에도 기본 정보는 유지하고 스탯만 None으로 설정
                            player_data['batterStats'] = None
                            player_data['pitcherStats'] = None
                            yield player_data
                            continue

                        # 스탯 파싱
                        stats_dict, _, is_batter, is_pitcher = parse_player_stats(combined_raw_stats)

                        # 선수 타입에 따라 스탯 저장 (엔티티 구조에 맞춰 하나의 딕셔너리로)
                        if is_pitcher:
                            player_data['pitcherStats'] = stats_dict
                            player_data['batterStats'] = None
                        elif is_batter:
                            player_data['batterStats'] = stats_dict
                            player_data['pitcherStats'] = None
                        else:
                            player_data['batterStats'] = None
                            player_data['pitcherStats'] = None
                        
                        yield player_data
                        
                        # 선수 처리 후 랜덤 대기 (서버 부하 방지 및 봇 탐지 회피)
                        time.sleep(random.uniform(0.3, 0.7))
                        
                    except Exception as e:
                        print(f"    [에러] {i+1}번째 선수 ({player_name}) 처리 중 예외 발생: {e}")
                        save_error_debug_info(driver, i, player_name)
                        print(f"    [Original Error] {e.__class__.__name__}")
                        continue
                
                current_page_num += 1
                # 페이지 이동 후 랜덤 대기
                time.sleep(random.uniform(0.8, 1.2))
    
    finally:
        if 'driver' in locals() and driver:
            driver.quit()
        print("\n모든 선수 정보 수집 완료.")
