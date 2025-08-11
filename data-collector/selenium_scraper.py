import time
import random
import json
import requests
from bs4 import BeautifulSoup
import undetected_chromedriver as uc
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait, Select
from selenium.webdriver.support import expected_conditions as EC

# from app import producer # 최종적으로 Kafka에 보낼 때 주석 해제
def scrape_all_players_and_details():
    options = uc.ChromeOptions()

    profile_path = r"C:\Users\TJ-BU-707-P14\AppData\Local\Google\Chrome\User Data"
    options.add_argument(f'--user-data-dir={profile_path}')
    options.add_argument('--profile-directory=Profile 1')

    # options.add_argument('--headless')
    options.add_argument('--no-sandbox')
    options.add_argument('--disable-dev-shm-usage')

    driver = uc.Chrome(options=options)
    
    player_info_list = []
    final_collected_data = []
    # team_codes = ['LG', 'OB', 'HH', 'HT', 'SS', 'KT', 'SK', 'LT', 'NC', 'WO']
    team_codes = ['LG', 'OB']
    base_url = "https://www.koreabaseball.com"

    try:
        # --- 1단계: 모든 선수의 ID, 이름, '포지션' 수집 ---
        print("모든 선수의 ID와 포지션 목록 수집을 시작합니다...")
        search_url = f"{base_url}/Player/Search.aspx"
        driver.get(search_url)

        for team_code in team_codes:
            print(f"\n{team_code} 팀 목록 수집 시작...")
            Select(driver.find_element(By.ID, 'cphContents_cphContents_cphContents_ddlTeam')).select_by_value(team_code)
            time.sleep(random.uniform(1, 3)) # 팀 선택 후 페이지 로딩 대기
            
            while True:
                WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.CSS_SELECTOR, ".tEx tbody tr")))
                soup = BeautifulSoup(driver.page_source, 'html.parser')
                for row in soup.select(".tEx tbody tr"):
                    link_tag = row.select_one("td:nth-child(2) > a")
                    position_tag = row.select_one("td:nth-child(4)") # 포지션 정보가 있는 4번째 칸
                    if link_tag and position_tag and 'playerId=' in link_tag.get('href'):
                        player_id = int(link_tag.get('href').split('playerId=')[1])
                        player_name = link_tag.text.strip()
                        position = position_tag.text.strip()
                        player_info_list.append({'id': player_id, 'name': player_name, 'position': position})
                try:
                    active_page_num = int(driver.find_element(By.CSS_SELECTOR, ".paging a.on").text)
                    next_page_button = driver.find_element(By.LINK_TEXT, str(active_page_num + 1))
                    driver.execute_script("arguments[0].click();", next_page_button)
                    time.sleep(random.uniform(2, 3))
                except Exception:
                    break
        
        print(f"\n총 {len(player_info_list)}명의 선수 ID 수집 완료. 상세 정보 수집을 시작합니다.")

        # --- 2단계: 수집된 목록을 순회하며 '포지션'에 맞게 상세 정보 수집 ---
        for i, player_info in enumerate(player_info_list):
            player_id = player_info['id']
            player_name = player_info['name']
            position = player_info['position']
            
            print(f"({i+1}/{len(player_info_list)}) {player_name}({player_id}) / {position} 정보 수집 중...")

            # [수정] 포지션에 따라 올바른 URL로 이동
            if position == '투수':
                detail_url = f"{base_url}/Player/PitcherDetail/Basic.aspx?playerId={player_id}"
            else:
                detail_url = f"{base_url}/Player/HitterDetail/Basic.aspx?playerId={player_id}"
            driver.get(detail_url)
            
            # [수정] 공통적으로 존재하는 프로필 영역을 기다림
            WebDriverWait(driver, 10).until(
                EC.presence_of_element_located((By.ID, "cphContents_cphContents_cphContents_playerProfile"))
            )
            
            soup = BeautifulSoup(driver.page_source, 'html.parser')
            
            player_data = {'id': player_id}
            # ... (이전과 동일하게 상세 프로필 정보 수집 로직) ...
            
            batter_stats_dto = None
            pitcher_stats_dto = None
            
            # 타자 스탯 테이블 확인 및 파싱
            batter_table = soup.select_one("#cphContents_cphContents_cphContents_ucHitterRecord_dgvRecord")
            if batter_table:
                batter_stats_dto = {}
                # (타자 스탯 파싱 로직)
            
            # 투수 스탯 테이블 확인 및 파싱
            pitcher_table = soup.select_one("#cphContents_cphContents_cphContents_ucPitcherRecord_dgvRecord")
            if pitcher_table:
                pitcher_stats_dto = {}
                # (투수 스탯 파싱 로직)
            
            player_data['batterStats'] = batter_stats_dto
            player_data['pitcherStats'] = pitcher_stats_dto
            final_collected_data.append(player_data)
            time.sleep(random.uniform(0.5, 1))

    finally:
        driver.quit()
        print("\n모든 선수 정보 수집 완료.")
    
    return final_collected_data

# ... (run_player_loading_task 함수와 if __name__ == '__main__' 부분은 동일) ...
def run_player_loading_task():
    all_players = scrape_all_players_and_details()
    # ... (콘솔 출력 및 파일 저장)
if __name__ == '__main__':
    run_player_loading_task()