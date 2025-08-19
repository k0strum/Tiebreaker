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
from utils.kafka_producer import create_kafka_producer

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
        'birthday': None,
        'heightWeight': None,
        'draftRank': None,
        'backNumber': None,
        'position': None,
        'career': None,
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

def find_daily_record_link(driver, wait):
    """
    일자별기록 링크를 찾는 함수 (여러 방법 시도)
    """
    selectors = [
        (By.LINK_TEXT, "일자별기록"),
        (By.PARTIAL_LINK_TEXT, "일자별"),
        (By.CSS_SELECTOR, "a[href*='Daily.aspx']"),
        (By.CSS_SELECTOR, "a[href*='daily']"),
        (By.XPATH, "//a[contains(text(), '일자별')]"),
        (By.XPATH, "//a[contains(@href, 'Daily')]")
    ]
    
    for selector_type, selector_value in selectors:
        try:
            element = wait.until(EC.element_to_be_clickable((selector_type, selector_value)))
            print(f"    [정보] 일자별기록 링크 발견: {selector_type} = {selector_value}")
            return element
        except TimeoutException:
            continue
    
    return None

def parse_pitcher_monthly_stats(soup):
    """
    투수 월별 기록 파싱 함수
    """
    monthly_records = []
    
    # 올바른 테이블 선택 (div.tbl-type02 내부의 테이블 또는 tbl tt mb5 클래스)
    tables = soup.select('div.tbl-type02 table, table.tbl.tt.mb5')
    
    for table_idx, table in enumerate(tables):
        # 테이블 내용 확인
        table_text = table.get_text(strip=True)
        if "월" not in table_text or "합계" not in table_text:
            continue
        
        current_month = None
        current_month_data = None
        
        # 1. thead에서 월 헤더 찾기
        thead = table.select_one('thead')
        if thead:
            month_header = thead.select_one('th')
            if month_header and "월" in month_header.text:
                try:
                    # "6월" 같은 형태에서 월 번호 추출
                    month_text = month_header.text.strip()
                    month_part = month_text.split("월")[0] + "월"
                    month_num = int(month_part.replace("월", ""))
                    current_month = month_num
                    current_month_data = {
                        'year': 2025,
                        'month': current_month,
                        'games': 0,
                        'inningsPitchedInteger': 0,
                        'inningsPitchedFraction': 0,
                        'strikeouts': 0,
                        'runsAllowed': 0,
                        'earnedRuns': 0,
                        'hitsAllowed': 0,
                        'homeRunsAllowed': 0,
                        'totalBattersFaced': 0,
                        'walksAllowed': 0,
                        'hitByPitch': 0,
                        'wins': 0,
                        'losses': 0,
                        'saves': 0,
                        'holds': 0
                    }
                except ValueError:
                    continue
        
        # 2. tbody에서 개별 경기 행 먼저 처리 (경기수/승패/세/홀)
        tbody = table.select_one('tbody')
        if tbody and current_month_data:
            rows = tbody.select('tr')
            for row in rows:
                cells = row.select('td')
                if len(cells) < 2:
                    continue
                first_cell_text = cells[0].text.strip()
                if "." in first_cell_text and len(first_cell_text) == 5:
                    current_month_data['games'] += 1
                    # 결과 칼럼 인덱스: 3 (구분 다음 칸)
                    result_cell = cells[3].text.strip() if len(cells) > 3 else ""
                    if result_cell == "승":
                        current_month_data['wins'] += 1
                    elif result_cell == "패":
                        current_month_data['losses'] += 1
                    elif result_cell == "세":
                        current_month_data['saves'] += 1
                    elif result_cell == "홀":
                        current_month_data['holds'] += 1

        # 3. tfoot에서 합계 행 찾기 (고정 컬럼 순서 기반 매핑: ERA1,TBF,IP,H,HR,BB,HBP,SO,R,ER,ERA2)
        tfoot = table.select_one('tfoot.play_record')
        if tfoot and current_month_data:
            for row in tfoot.select('tr'):
                cells = row.select('th, td')
                if len(cells) < 2:
                    continue
                texts = [c.get_text(strip=True) for c in cells]
                if not texts or '합계' not in texts[0]:
                    continue
                
                # 첫 셀(합계) 제외 후 고정 컬럼 순서로 정렬
                data_values = texts[1:]
                expected_cols = ['ERA1','TBF','IP','H','HR','BB','HBP','SO','R','ER','ERA2']
                # 길이 보정
                max_len = min(len(expected_cols), len(data_values))
                col_to_val = {expected_cols[i]: data_values[i] for i in range(max_len)}

                def to_int(s):
                    return int(s) if s.isdigit() else 0

                # TBF
                if 'TBF' in col_to_val:
                    current_month_data['totalBattersFaced'] = to_int(col_to_val['TBF'])

                # IP 파싱
                if 'IP' in col_to_val:
                    ip_text = col_to_val['IP']
                    if ' ' in ip_text:
                        parts = ip_text.split(' ')
                        try:
                            current_month_data['inningsPitchedInteger'] = int(parts[0])
                        except:
                            current_month_data['inningsPitchedInteger'] = 0
                        if len(parts) > 1 and '/' in parts[1]:
                            frac = parts[1].split('/')[0]
                            try:
                                current_month_data['inningsPitchedFraction'] = int(frac)
                            except:
                                current_month_data['inningsPitchedFraction'] = 0
                    else:
                        try:
                            current_month_data['inningsPitchedInteger'] = int(ip_text)
                        except:
                            current_month_data['inningsPitchedInteger'] = 0

                # 카운팅 지표
                if 'H' in col_to_val:
                    current_month_data['hitsAllowed'] = to_int(col_to_val['H'])
                if 'HR' in col_to_val:
                    current_month_data['homeRunsAllowed'] = to_int(col_to_val['HR'])
                if 'BB' in col_to_val:
                    current_month_data['walksAllowed'] = to_int(col_to_val['BB'])
                if 'HBP' in col_to_val:
                    current_month_data['hitByPitch'] = to_int(col_to_val['HBP'])
                if 'SO' in col_to_val:
                    current_month_data['strikeouts'] = to_int(col_to_val['SO'])
                if 'R' in col_to_val:
                    current_month_data['runsAllowed'] = to_int(col_to_val['R'])
                if 'ER' in col_to_val:
                    current_month_data['earnedRuns'] = to_int(col_to_val['ER'])

                monthly_records.append(current_month_data)
                current_month_data = None
                break  # 합계 행 1개만 처리
        
    
    return monthly_records

def parse_batter_monthly_stats(soup):
    """
    타자 월별 기록 파싱 함수
    """
    monthly_records = []
    
    # 올바른 테이블 선택 (div.tbl-type02 내부의 테이블 또는 tbl tt mb5 클래스)
    tables = soup.select('div.tbl-type02 table, table.tbl.tt.mb5')
    
    for table_idx, table in enumerate(tables):
        # 테이블 내용 확인
        table_text = table.get_text(strip=True)
        if "월" not in table_text or "합계" not in table_text:
            continue
        
        current_month = None
        current_month_data = None
        
        # 1. thead에서 월 헤더 찾기
        thead = table.select_one('thead')
        if thead:
            month_header = thead.select_one('th')
            if month_header and "월" in month_header.text:
                try:
                    # "6월" 같은 형태에서 월 번호 추출
                    month_text = month_header.text.strip()
                    month_part = month_text.split("월")[0] + "월"
                    month_num = int(month_part.replace("월", ""))
                    current_month = month_num
                    current_month_data = {
                        'year': 2025,
                        'month': current_month,
                        'games': 0,  # 개별 경기 수 카운트용
                        'plateAppearances': 0,
                        'atBats': 0,
                        'hits': 0,
                        'doubles': 0,
                        'triples': 0,
                        'homeRuns': 0,
                        'runsBattedIn': 0,
                        'runs': 0,
                        'walks': 0,
                        'hitByPitch': 0,
                        'strikeouts': 0,
                        'stolenBases': 0,
                        'caughtStealing': 0,
                        'groundedIntoDoublePlay': 0
                    }
                except ValueError:
                    continue
            
        # 2. tbody에서 개별 경기 행 먼저 처리 (경기수 카운트)
        tbody = table.select_one('tbody')
        if tbody and current_month_data:
            rows = tbody.select('tr')
            for row in rows:
                cells = row.select('td')
                if len(cells) < 2:
                    continue
                first_cell_text = cells[0].text.strip()
                if "." in first_cell_text and len(first_cell_text) == 5:
                    current_month_data['games'] += 1

        # 3. tfoot에서 합계 행 찾기 (고정 컬럼 순서 기반 매핑: AVG1,PA,AB,R,H,2B,3B,HR,RBI,SB,CS,BB,HBP,SO,GDP,AVG2)
        tfoot = table.select_one('tfoot.play_record')
        if tfoot and current_month_data:
            for row in tfoot.select('tr'):
                cells = row.select('th, td')
                if len(cells) < 2:
                    continue
                texts = [c.get_text(strip=True) for c in cells]
                if not texts or '합계' not in texts[0]:
                    continue
                
                # 첫 셀(합계) 제외 후 고정 컬럼 순서로 정렬
                data_values = texts[1:]
                expected_cols = ['AVG1','PA','AB','R','H','2B','3B','HR','RBI','SB','CS','BB','HBP','SO','GDP','AVG2']
                max_len = min(len(expected_cols), len(data_values))
                col_to_val = {expected_cols[i]: data_values[i] for i in range(max_len)}

                def to_int(s):
                    return int(s) if s.isdigit() else 0

                mapping = {
                    'PA': 'plateAppearances',
                    'AB': 'atBats',
                    'R': 'runs',
                    'H': 'hits',
                    '2B': 'doubles',
                    '3B': 'triples',
                    'HR': 'homeRuns',
                    'RBI': 'runsBattedIn',
                    'SB': 'stolenBases',
                    'CS': 'caughtStealing',
                    'BB': 'walks',
                    'HBP': 'hitByPitch',
                    'SO': 'strikeouts',
                    'GDP': 'groundedIntoDoublePlay',
                }

                for col, field in mapping.items():
                    if col in col_to_val:
                        current_month_data[field] = to_int(col_to_val[col])

                monthly_records.append(current_month_data)
                current_month_data = None
                break  # 합계 행 1개만 처리
        
        # tbody는 위에서 처리 완료
    
    return monthly_records

def collect_monthly_stats_improved(driver, player_id, is_batter, is_pitcher):
    """
    개선된 월별 기록 수집 함수
    """
    monthly_stats = {'batter': [], 'pitcher': []}
    wait = WebDriverWait(driver, 8)
    
    try:
        # 1. 일자별기록 링크 찾기
        daily_link = find_daily_record_link(driver, wait)
        if not daily_link:
            print(f"    [경고] 일자별기록 링크를 찾을 수 없습니다.")
            return monthly_stats
        
        # 2. 현재 URL 저장 (복귀용)
        current_url = driver.current_url
        
        # 3. 일자별기록 버튼 클릭
        print(f"    [정보] 일자별기록 페이지로 이동 중...")
        driver.execute_script("arguments[0].click();", daily_link)
        
        # 4. 페이지 로딩 대기
        time.sleep(random.uniform(1.0, 1.5))
        
        # 5. 새로운 페이지 확인
        try:
            wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, "table")))
            print(f"    [정보] 일자별기록 페이지 로딩 완료")
        except TimeoutException:
            print(f"    [경고] 일자별기록 페이지 로딩 타임아웃")
            return monthly_stats
        
        # 6. 월별 기록 파싱
        soup = BeautifulSoup(driver.page_source, 'html.parser')
        
        if is_pitcher:
            monthly_stats['pitcher'] = parse_pitcher_monthly_stats(soup)
            print(f"    [정보] 투수 월별 기록 수집 완료: {len(monthly_stats['pitcher'])}개월")
        elif is_batter:
            monthly_stats['batter'] = parse_batter_monthly_stats(soup)
            print(f"    [정보] 타자 월별 기록 수집 완료: {len(monthly_stats['batter'])}개월")
        
        # 7. 원래 페이지로 복귀 (선택사항)
        # driver.get(current_url)
        # time.sleep(random.uniform(1.0, 1.5))
        
    except Exception as e:
        print(f"    [경고] 월별 기록 수집 중 오류: {e}")
        # 오류 발생 시 원래 페이지로 복귀 시도
        try:
            driver.get(current_url)
        except:
            pass
    
    return monthly_stats

def send_player_data_to_kafka(producer, player_data):
    """
    선수 데이터를 Kafka로 전송하는 함수 - 연도별/월별 기록 분리
    """
    config = get_config()
    
    try:
        # 1. 연도별 기록 전송 (기본 정보 포함)
        yearly_data = {
            'id': player_data['id'],
            'playerName': player_data['playerName'],
            'teamName': player_data['teamName'],
            'imageUrl': player_data['imageUrl'],
            'birthday': player_data.get('birthday'),
            'heightWeight': player_data.get('heightWeight'),
            'draftRank': player_data.get('draftRank'),
            'backNumber': player_data.get('backNumber'),
            'position': player_data.get('position'),
            'career': player_data.get('career'),
            'batterStats': player_data['batterStats'],
            'pitcherStats': player_data['pitcherStats']
        }
        
        producer.send(
            config['kafka']['topic_player_yearly'],
            yearly_data
        )
        print(f"    [Kafka] 연도별 기록 전송 완료: {player_data['playerName']}")
        
        # 2. 전송 완료 대기 (연도별 데이터가 먼저 처리되도록)
        producer.flush()
        
        # 3. 잠시 대기 후 월별 기록 전송 (선수 기본 정보가 DB에 저장될 시간 확보)
        import time
        time.sleep(0.5)
        
        # 4. 월별 기록 전송 (월별 데이터가 있는 경우)
        if player_data['monthlyStats']['batter'] or player_data['monthlyStats']['pitcher']:
            monthly_data = {
                'playerId': player_data['id'],
                'batter': player_data['monthlyStats']['batter'],
                'pitcher': player_data['monthlyStats']['pitcher']
            }
            
            producer.send(
                config['kafka']['topic_player_monthly'],
                monthly_data
            )
            print(f"    [Kafka] 월별 기록 전송 완료: {player_data['playerName']} ({len(player_data['monthlyStats']['batter'] + player_data['monthlyStats']['pitcher'])}개월)")
            
            # 월별 데이터도 전송 완료 대기
            producer.flush()
        
    except Exception as e:
        print(f"    [Kafka 오류] {player_data['playerName']} 데이터 전송 실패: {e}")

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
    producer = None
    
    try:
        # Kafka Producer 초기화
        producer = create_kafka_producer([config['kafka']['bootstrap_servers']])
        if not producer:
            print("[ERROR] Kafka Producer 초기화 실패")
            return
        
        driver = webdriver.Chrome(options=options)
        driver.set_page_load_timeout(30)
        print("[DEBUG] WebDriver 초기화에 성공했습니다!")
    except Exception as e:
        print(f"[CRITICAL] 초기화 중 심각한 오류가 발생했습니다: {e}")
        return

    try:
        # team_codes = ['LG', 'OB', 'HH', 'HT', 'SS', 'KT', 'SK', 'LT', 'NC', 'WO']
        team_codes = ['SK', 'OB', 'HH', 'HT', 'SS', 'KT', 'LG', 'LT', 'NC', 'WO']
        search_url = "https://www.koreabaseball.com/Player/Search.aspx"
        wait = WebDriverWait(driver, 15)
        
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
                            time.sleep(random.uniform(0.8, 1.2))
                        except Exception as click_error:
                            print(f"    [경고] {player_name} 선수 링크 클릭 실패: {click_error}")
                            player_data = create_default_player_data(player_name, team_name)
                            send_player_data_to_kafka(producer, player_data)
                            continue
                        
                        # 선수 상세 페이지 로딩 대기
                        try:
                            wait.until(EC.presence_of_element_located((By.CLASS_NAME, "player_basic")))
                            # 추가 랜덤 대기 시간 (페이지 완전 로딩)
                            time.sleep(random.uniform(0.3, 0.6))
                        except TimeoutException:
                            print(f"    [경고] {player_name} 선수 상세 페이지 로딩 타임아웃.")
                            current_url = driver.current_url
                            if "Search.aspx" in current_url:
                                print(f"    [확인] 여전히 검색 페이지에 있습니다: {current_url}")
                                player_data = create_default_player_data(player_name, team_name)
                                send_player_data_to_kafka(producer, player_data)
                                continue
                            else:
                                print(f"    [재시도] 다른 페이지로 이동했습니다: {current_url}")
                                try:
                                    wait.until(EC.presence_of_element_located((By.CLASS_NAME, "player_basic")))
                                    time.sleep(random.uniform(0.3, 0.6))
                                except TimeoutException:
                                    print(f"    [최종 실패] {player_name} 선수 페이지 로딩 실패")
                                    player_data = create_default_player_data(player_name, team_name)
                                    send_player_data_to_kafka(producer, player_data)
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
                            player_data['monthlyStats'] = {'batter': [], 'pitcher': []}
                            
                            send_player_data_to_kafka(producer, player_data)
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
                            player_data['monthlyStats'] = {'batter': [], 'pitcher': []}
                            send_player_data_to_kafka(producer, player_data)
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

                        # === 월별 기록 수집 추가 ===
                        monthly_stats = collect_monthly_stats_improved(driver, player_id, is_batter, is_pitcher)
                        player_data['monthlyStats'] = monthly_stats
                        
                        # Kafka로 데이터 전송
                        send_player_data_to_kafka(producer, player_data)
                        
                        # 선수 처리 후 랜덤 대기 (서버 부하 방지 및 봇 탐지 회피)
                        time.sleep(random.uniform(0.1, 0.3))
                        
                    except Exception as e:
                        print(f"    [에러] {i+1}번째 선수 ({player_name}) 처리 중 예외 발생: {e}")
                        save_error_debug_info(driver, i, player_name)
                        print(f"    [Original Error] {e.__class__.__name__}")
                        continue
                
                current_page_num += 1
                # 페이지 이동 후 랜덤 대기
                time.sleep(random.uniform(0.3, 0.6))
    
    finally:
        if 'driver' in locals() and driver:
            driver.quit()
        if 'producer' in locals() and producer:
            producer.close()
        print("\n모든 선수 정보 수집 완료.")
