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
from datetime import datetime
from typing import Optional, List, Dict, Tuple

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

# === 월간 타자 합계 파싱 유틸 ===
HEADER_TO_FIELD_MONTHLY = {
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

def _to_int_safe(v: str) -> int:
    if v is None:
        return 0
    v = v.strip()
    if v in ('', '-', '–'):
        return 0
    try:
        return int(v)
    except ValueError:
        try:
            return int(float(v))
        except Exception:
            return 0

def _extract_month_from_thead(table_el) -> Optional[int]:
    ths = table_el.select('thead th')
    if not ths:
        return None
    t = ths[0].get_text(strip=True).replace(' ', '')
    if t.endswith('월'):
        try:
            return int(t[:-1])
        except Exception:
            return None
    return None

def _select_monthly_divs_by_header(soup: BeautifulSoup) -> List:
    """player_records 내 월간 표(div.tbl-type02.tbl-type02-pd0)만 동적으로 선별.
    thead 첫 번째 th 텍스트가 'N월'인 경우만 채택 (월 수 변동 대응)
    """
    pr = soup.select_one('#contents > div.sub-content > div.player_records')
    if not pr:
        # 폴백: 어디에 있든 첫 번째 player_records 사용
        pr = soup.find('div', class_='player_records')
        if not pr:
            return []
    # 직계 자식만 안전하게 탐색
    children = pr.find_all('div', recursive=False)
    results = []
    for div in children:
        classes = div.get('class', [])
        if 'tbl-type02' not in classes or 'tbl-type02-pd0' not in classes:
            continue
        table = div.select_one('table.tbl.tt')
        if not table:
            continue
        ths = table.select('thead th')
        if not ths:
            continue
        h0 = ths[0].get_text(strip=True).replace(' ', '')
        if h0.endswith('월'):
            results.append(div)
    return results

def _parse_batter_monthly_total_from_div(month_div, year: Optional[int]) -> Optional[dict]:
    table = month_div.select_one('table.tbl.tt')
    if not table:
        return None

    month_num = _extract_month_from_thead(table)
    if not month_num:
        return None

    total_row = table.select_one('tfoot.play_record tr')
    if not total_row:
        return None

    total_cells = [c.get_text(strip=True) for c in total_row.select('th, td')]
    headers = [th.get_text(strip=True) for th in table.select('thead th')]

    # headers: [월, 상대, AVG1, PA, AB, R, H, 2B, 3B, HR, RBI, SB, CS, BB, HBP, SO, GDP, AVG2]
    # totals : [합계(2칸), AVG1, PA, AB, R, H, 2B, 3B, HR, RBI, SB, CS, BB, HBP, SO, GDP, AVG2]
    core_headers = headers[2:]
    core_totals = total_cells[1:]
    if len(core_totals) < len(core_headers):
        core_headers = core_headers[:len(core_totals)]

    header_to_total = dict(zip(core_headers, core_totals))

    stats = {
        'year': year,
        'month': month_num,
        # 요청: 헤더/풋터 제외 tbody 행 개수를 그대로 게임수로 사용
        'games': len(table.select('tbody tr')),
        'plateAppearances': _to_int_safe(header_to_total.get('PA', '0')),
        'atBats': _to_int_safe(header_to_total.get('AB', '0')),
        'hits': _to_int_safe(header_to_total.get('H', '0')),
        'doubles': _to_int_safe(header_to_total.get('2B', '0')),
        'triples': _to_int_safe(header_to_total.get('3B', '0')),
        'homeRuns': _to_int_safe(header_to_total.get('HR', '0')),
        'runsBattedIn': _to_int_safe(header_to_total.get('RBI', '0')),
        'runs': _to_int_safe(header_to_total.get('R', '0')),
        'walks': _to_int_safe(header_to_total.get('BB', '0')),
        'hitByPitch': _to_int_safe(header_to_total.get('HBP', '0')),
        'strikeouts': _to_int_safe(header_to_total.get('SO', '0')),
        'stolenBases': _to_int_safe(header_to_total.get('SB', '0')),
        'caughtStealing': _to_int_safe(header_to_total.get('CS', '0')),
        'groundedIntoDoublePlay': _to_int_safe(header_to_total.get('GDP', '0')),
    }
    try:
        print(f"    [월간-타자] {month_num}월: 경기수 {stats['games']}")
    except Exception:
        pass
    return stats

def parse_batter_monthly_totals_for_season(soup: BeautifulSoup, year: Optional[int]) -> List[dict]:
    month_divs = _select_monthly_divs_by_header(soup)
    results = []
    for div in month_divs:
        data = _parse_batter_monthly_total_from_div(div, year=year)
        if data:
            results.append(data)
    results.sort(key=lambda x: x['month'])
    return results

# === 월간 투수 합계 파싱 유틸 ===
def _parse_ip_to_parts(ip_text: str) -> Tuple[int, int]:
    """IP 문자열을 정수, 분수(0/1/2)로 분해. 예: '5.2' -> (5, 2). '-', '' -> (0, 0)"""
    if not ip_text:
        return 0, 0
    t = ip_text.strip()
    if t in ('-', '–'):
        return 0, 0
    try:
        if '.' in t:
            whole, frac = t.split('.', 1)
            whole_int = int(whole) if whole else 0
            frac_int = int(frac[:1]) if frac else 0
            # 안전장치: 0/1/2 외 값은 0 처리
            if frac_int not in (0, 1, 2):
                frac_int = 0
            return whole_int, frac_int
        else:
            return int(t), 0
    except Exception:
        return 0, 0

def _is_pitcher_monthly_table(table_el) -> bool:
    headers = [th.get_text(strip=True) for th in table_el.select('thead th')]
    if not headers:
        return False
    # Pitcher monthly key headers
    needed = {'TBF', 'IP', 'ER', 'SO', 'ERA1'}
    return needed.issubset(set(h.replace(' ', '') for h in headers))

def _parse_pitcher_monthly_total_from_div(month_div, year: Optional[int]) -> Optional[dict]:
    table = month_div.select_one('table.tbl.tt')
    if not table or not _is_pitcher_monthly_table(table):
        return None

    month_num = _extract_month_from_thead(table)
    if not month_num:
        return None

    total_row = table.select_one('tfoot.play_record tr')
    if not total_row:
        return None

    total_cells = [c.get_text(strip=True) for c in total_row.select('th, td')]
    headers = [th.get_text(strip=True) for th in table.select('thead th')]

    # headers: [월, 상대, 구분, 결과, ERA1, TBF, IP, H, HR, BB, HBP, SO, R, ER, ERA2]
    # totals : [합계(3~4칸), ERA1, TBF, IP, H, HR, BB, HBP, SO, R, ER, ERA2] (구분/결과는 없음)
    # -> headers에서 ERA1 이후의 핵심 지표들을 totals 대비로 매핑
    # ERA1은 계산 지표라 저장 안 함. 순수 카운트 + IP, SO, R, ER 등만 처리
    # core_headers를 ERA1 포함으로 잡고, core_totals는 합계 셀 다음부터
    # 합계 셀 개수는 사이트 구조 변화 가능성 있으므로 값 정렬로 보정
    # 간단 접근: 헤더에서 ERA1의 인덱스를 찾고, 그 이후 헤더들과 totals 마지막 길이를 맞춰 zip
    try:
        era1_idx = headers.index('ERA1')
    except ValueError:
        # 헤더 이름에 공백/줄바꿈이 섞일 수 있어 대체 탐색
        normalized = [h.replace(' ', '') for h in headers]
        era1_idx = normalized.index('ERA1') if 'ERA1' in normalized else -1
    if era1_idx == -1:
        return None

    core_headers = headers[era1_idx:]  # ERA1부터 끝까지
    # totals의 앞쪽 합계/병합 셀(구분,결과 등)은 무시하고 뒤에서 core_headers 길이에 맞춰 슬라이싱
    # 다만 core_headers에는 ERA2 같은 계산 지표도 포함되므로 이후 필터링
    if len(total_cells) >= len(core_headers):
        core_totals = total_cells[-len(core_headers):]
    else:
        # 길이가 짧으면 앞쪽 합계 셀을 하나 덜 무시하고 다시 맞춤
        core_totals = total_cells[1:]
        if len(core_totals) < len(core_headers):
            core_headers = core_headers[:len(core_totals)]

    header_to_total = dict(zip(core_headers, core_totals))

    ip_text = header_to_total.get('IP') or header_to_total.get('이닝') or '0'
    ip_i, ip_f = _parse_ip_to_parts(ip_text)

    stats = {
        'year': year,
        'month': month_num,
        'games': len(table.select('tbody tr')),
        'inningsPitchedInteger': ip_i,
        'inningsPitchedFraction': ip_f,
        'strikeouts': _to_int_safe(header_to_total.get('SO', '0')),
        'runsAllowed': _to_int_safe(header_to_total.get('R', '0')),
        'earnedRuns': _to_int_safe(header_to_total.get('ER', '0')),
        'hitsAllowed': _to_int_safe(header_to_total.get('H', '0')),
        'homeRunsAllowed': _to_int_safe(header_to_total.get('HR', '0')),
        'totalBattersFaced': _to_int_safe(header_to_total.get('TBF', '0')),
        'walksAllowed': _to_int_safe(header_to_total.get('BB', '0')),
        'hitByPitch': _to_int_safe(header_to_total.get('HBP', '0')),
        # 승패/세이브/홀드는 월간 표에 없으면 0
        'wins': 0,
        'losses': 0,
        'saves': 0,
        'holds': 0,
    }
    try:
        print(f"    [월간-투수] {month_num}월: 경기수 {stats['games']}")
    except Exception:
        pass
    return stats

def parse_pitcher_monthly_totals_for_season(soup: BeautifulSoup, year: Optional[int]) -> List[dict]:
    month_divs = _select_monthly_divs_by_header(soup)
    results = []
    for div in month_divs:
        data = _parse_pitcher_monthly_total_from_div(div, year=year)
        if data:
            results.append(data)
    results.sort(key=lambda x: x['month'])
    return results

# === Daily(일자별기록) 탭으로 이동해 soup 확보 ===
def _activate_pitcher_tab(driver, wait) -> bool:
    try:
        # 상위 탭(타자/투수) 중 '투수'가 표시/활성된 앵커를 찾아 클릭
        target = None
        try:
            candidates = driver.find_elements(By.CSS_SELECTOR, "#contents .player_info .tab-depth1 a")
            for el in candidates:
                try:
                    if el.is_displayed() and '투수' in (el.text or '').strip():
                        target = el
                        break
                except Exception:
                    continue
        except Exception:
            pass
        if target is None:
            try:
                candidates = driver.find_elements(By.PARTIAL_LINK_TEXT, '투수')
                for el in candidates:
                    try:
                        if el.is_displayed():
                            target = el
                            break
                    except Exception:
                        continue
            except Exception:
                pass
        if target is None:
            return False
        driver.execute_script('arguments[0].scrollIntoView({block: "center"});', target)
        try:
            wait.until(EC.element_to_be_clickable(target))
            target.click()
        except Exception:
            driver.execute_script('arguments[0].click();', target)
        # 하위 탭 노출 대기
        try:
            wait.until(EC.visibility_of_element_located((By.CSS_SELECTOR, '#contents .player_info .tab-depth2')))
        except Exception:
            pass
        return True
    except Exception:
        return False

def _get_daily_records_soup(driver, wait) -> Optional[BeautifulSoup]:
    """선수 기본 페이지에서 셀레니움 클릭으로 '일자별기록' 탭으로 이동 후 soup 반환.
    직접 URL 이동은 차단 페이지를 유발하므로 사용하지 않음.
    """
    try:
        prev_url = driver.current_url

        # 사용자가 제공한 정확 CSS 우선
        el = None
        exact_sel = '#contents > div.sub-content > div.player_info > div.tab-depth2 > ul > li:nth-child(3) > a'
        try:
            candidate = driver.find_element(By.CSS_SELECTOR, exact_sel)
            if candidate and candidate.is_displayed() and candidate.is_enabled():
                el = candidate
        except Exception:
            el = None
        # 폴백: tab-depth2 내부 Daily 링크 중 표시/활성된 것
        if el is None:
            try:
                candidates = driver.find_elements(By.CSS_SELECTOR, '#contents .player_info .tab-depth2 a[href*="Daily.aspx"]')
                for c in candidates:
                    try:
                        if c.is_displayed() and c.is_enabled():
                            el = c
                            break
                    except Exception:
                        continue
            except Exception:
                el = None
        # 폴백: 텍스트 기반
        if el is None:
            try:
                candidates = driver.find_elements(By.PARTIAL_LINK_TEXT, '일자별기록')
                for c in candidates:
                    try:
                        if c.is_displayed() and c.is_enabled():
                            el = c
                            break
                    except Exception:
                        continue
            except Exception:
                el = None
        if el is None:
            print("    [경고] '일자별기록' 탭 링크를 찾을 수 없습니다.")
            return None

        driver.execute_script('arguments[0].scrollIntoView({block: "center"});', el)
        try:
            wait.until(EC.element_to_be_clickable(el))
            el.click()
        except Exception:
            driver.execute_script('arguments[0].click();', el)

        # URL 변경 또는 Daily 콘텐츠 로드 대기
        try:
            wait.until(lambda d: ('Daily.aspx' in d.current_url) or (len(d.find_elements(By.XPATH, "//*[contains(normalize-space(),'일자별 성적')]")) > 0))
        except Exception:
            pass
        # 일자별 페이지 고유 마커 대기
        try:
            wait.until(EC.presence_of_element_located((By.XPATH, "//*[contains(normalize-space(),'일자별 성적')]")))
        except Exception:
            wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, 'div.player_records')))
        print("    [정보] Daily 페이지 진입 확인")
        return BeautifulSoup(driver.page_source, 'html.parser')
    except TimeoutException:
        print("    [경고] '일자별기록' 탭 로딩 타임아웃")
        return None
    except Exception as e:
        print(f"    [경고] '일자별기록' 탭 이동 실패: {e}")
        return None

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
        team_codes = ['NC', 'OB']
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
                                'imageUrl': None,
                                'batterStats': None,
                                'pitcherStats': None
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
                                    'imageUrl': None,
                                    'batterStats': None,
                                    'pitcherStats': None
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
                                        'imageUrl': None,
                                        'batterStats': None,
                                        'pitcherStats': None
                                    }
                                    yield player_data
                                    continue
                        
                        soup = BeautifulSoup(driver.page_source, 'html.parser')

                        # 이미지 URL 수집
                        image_url = None
                        try:
                            # 'player_basic' 클래스 내부의 img 태그를 찾습니다.
                            img_tag = soup.select_one('.player_basic .photo img')
                            if img_tag and img_tag.has_attr('src'):
                                image_url = img_tag['src']
                                # URL이 //로 시작하는 경우 https:를 붙여줍니다.
                                if image_url.startswith('//'):
                                    image_url = 'https:' + image_url
                                print(f"    [정보] {player_name} 선수 이미지 URL 수집: {image_url}")
                            else:
                                print(f"    [경고] {player_name} 선수의 이미지 태그를 찾을 수 없습니다.")
                        except Exception as e:
                            print(f"    [경고] {player_name} 선수의 이미지 URL을 찾을 수 없습니다: {e}")
                            image_url = None

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

                        player_data = {
                            'id': player_id, 
                            'playerName': player_name, 
                            'teamName': team_name,
                            'imageUrl': image_url
                        }

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

                        # 기본 페이지 테이블이 없어도 '일자별기록'에서 월간을 파싱할 수 있으므로 조기 종료하지 않음
                        if not all_stats_tables or "기록이 없습니다" in all_stats_tables[0].text:
                            print(f"    [정보] {player_name} 기본 페이지는 파싱할 스탯 테이블이 없음. 일자별기록에서 월간 시도.")
                            combined_raw_stats = {}
                        

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
                                            # 이닝은 문자열로 그대로 저장 (파싱은 백엔드에서 처리)
                                            if key == 'inningsPitched':
                                                base_stats_dict[key] = value
                                            elif '.' in value:
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

                        # 선수 타입에 따라 스탯 저장 (통합 형태로 전송)
                        # 공통: 일자별기록 탭에서 월간 합계 시도 (일자별기록 탭이 실제 월별 표 위치)
                        try:
                            current_year = datetime.now().year
                            daily_soup = _get_daily_records_soup(driver, wait)
                            # 롤백: 월간 파싱 로직 제거. 이후 단계에서는 월간 수집을 수행하지 않음.
                            b_monthlies, p_monthlies = [], []
                        except Exception as monthly_e:
                            print(f"    [경고] 월간 합계 파싱 실패(공통): {monthly_e}")
                            b_monthlies, p_monthlies = [], []

                        if is_pitcher:
                            # 투수 스탯: 기본 기록과 계산 지표를 하나로 통합
                            pitcher_stats_combined = base_stats_dict.copy()
                            pitcher_stats_combined.update(calculated_stats_dict)
                            player_data['pitcherStats'] = pitcher_stats_combined
                            player_data['batterStats'] = None
                        elif is_batter:
                            # 타자 스탯: 기본 기록과 계산 지표를 하나로 통합
                            batter_stats_combined = base_stats_dict.copy()
                            batter_stats_combined.update(calculated_stats_dict)
                            player_data['batterStats'] = batter_stats_combined
                            player_data['pitcherStats'] = None
                        else:
                            player_data['batterStats'] = None
                            player_data['pitcherStats'] = None
                        # 월간 관련 필드 제거 (완전 롤백)
                        player_data['batterMonthlyStats'] = None
                        player_data['pitcherMonthlyStats'] = None
                        
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
