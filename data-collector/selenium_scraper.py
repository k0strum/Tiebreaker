import time
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from bs4 import BeautifulSoup

def test_single_page_scrape():
    """
    LG 트윈스 선수 목록 1페이지만 가져와서 출력하는 테스트 함수
    """
    options = webdriver.ChromeOptions()
    # options.add_argument('--headless') # 테스트 중에는 이 옵션을 주석 처리하여 브라우저 창을 직접 보는 것이 좋음
    driver = webdriver.Chrome(options=options)

    scraped_players = []
    try:
        print("KBO 선수 검색 페이지로 이동합니다...")
        url = "https://www.koreabaseball.com/Player/Search.aspx"
        driver.get(url)

        # 'LG' 팀을 선택 (실제 사이트의 요소 ID나 선택자로 변경 필요)
        print("LG 트윈스 팀을 선택합니다...")
        driver.find_element(By.ID, 'cphContents_cphContents_cphContents_ddlTeam').click()
        driver.find_element(By.CSS_SELECTOR, "option[value='LG']").click()

        # 선수 목록 테이블이 로딩될 때까지 최대 10초 대기
        print("1페이지 선수 목록 로딩을 기다립니다...")
        WebDriverWait(driver, 10).until(
            EC.presence_of_element_located((By.CSS_SELECTOR, ".tEx tbody tr"))
        )
        print("로딩 완료. 데이터 추출을 시작합니다.")

        # 현재 페이지의 HTML을 BeautifulSoup으로 파싱
        soup = BeautifulSoup(driver.page_source, 'html.parser')
        
        player_rows = soup.select(".tEx tbody tr")
        for row in player_rows:
            try:
                link_tag = row.select_one("td:nth-child(2) > a")
                if link_tag and 'playerId=' in link_tag.get('href'):
                    player_id = int(link_tag.get('href').split('playerId=')[1])
                    player_name = link_tag.text.strip()
                    
                    # 추출된 데이터를 바로 print로 확인
                    player_data = {'id': player_id, 'name': player_name}
                    print(f"  -> 수집 성공: {player_data}")
                    scraped_players.append(player_data)

            except Exception as e:
                print(f"  -> 행 파싱 중 오류 발생: {e}")

    finally:
        print("테스트 종료. 브라우저를 닫습니다.")
        driver.quit()
    
    return scraped_players


# 이 파일을 직접 실행할 때만 테스트 코드가 돌아가도록 설정
if __name__ == '__main__':
    test_single_page_scrape()