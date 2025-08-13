# Selenium을 사용하는 함수도 다른 파일에 만들어 가져올 수 있습니다.
from selenium_scraper import scrape_all_players_and_details 
import time

def run_player_loading_task(producer):
    """
    모든 선수 마스터 데이터를 수집하여 Kafka로 전송하는 1회성 작업
    """
    # Kafka Producer가 정상적으로 생성되었는지 확인
    if not producer:
        print("Kafka Producer가 초기화되지 않았습니다. 작업을 중단합니다.")
        return

    print("선수 마스터 데이터 수집을 시작합니다...")

    # 3. 가져온 producer를 사용하여 각 선수 정보를 Kafka로 보냅니다.
    for player in scrape_all_players_and_details():
        print(f"  -> {player.get('playerName', '알 수 없는 선수')} 정보 전송 중...")
        producer.send('kbo-player-data', value=player)
        # time.sleep(0.1) # Kafka 성능에 따라 이 지연은 조절하거나 제거할 수 있습니다.

    producer.flush() # 모든 메시지가 전송되도록 보장
    print("모든 선수 마스터 데이터 전송 완료!")
    