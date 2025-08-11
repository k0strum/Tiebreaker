# 1. app.py에서 Kafka Producer를 부품처럼 가져옵니다.
from app import producer
# Selenium을 사용하는 함수도 다른 파일에 만들어 가져올 수 있습니다.
from selenium_scraper import get_all_players 
import time

def run_player_loading_task():
    """
    모든 선수 마스터 데이터를 수집하여 Kafka로 전송하는 1회성 작업
    """
    # Kafka Producer가 정상적으로 생성되었는지 확인
    if not producer:
        print("Kafka Producer가 초기화되지 않았습니다. 작업을 중단합니다.")
        return

    print("선수 마스터 데이터 수집을 시작합니다...")
    
    # 2. Selenium을 사용해 모든 선수 정보를 가져옵니다.
    all_players = get_all_players() # 이 함수는 선수 목록을 반환한다고 가정

    # 3. 가져온 producer를 사용하여 각 선수 정보를 Kafka로 보냅니다.
    for player in all_players:
        print(f"  -> {player['playerName']} 선수 정보 전송 중...")
        producer.send('kbo-player-master-data', value=player)
        time.sleep(0.1) # 너무 빠르게 보내지 않도록 약간의 지연

    producer.flush() # 모든 메시지가 전송되도록 보장
    print("모든 선수 마스터 데이터 전송 완료!")


if __name__ == '__main__':
    run_player_loading_task()