import logging
from apscheduler.schedulers.background import BackgroundScheduler
from collectors.player_collector import scrape_all_players_and_details
from utils.kafka_producer import create_kafka_producer

def schedule_player_collection():
    """
    선수 정보 데이터 수집을 스케줄링합니다.
    """
    def collect_and_send():
        """데이터를 수집하고 Kafka로 전송합니다."""
        try:
            producer = create_kafka_producer()
            if not producer:
                logging.error("Kafka Producer 생성 실패")
                return
            
            # 선수 데이터 수집 및 전송
            for player in scrape_all_players_and_details():
                logging.info(f"선수 정보 전송 중: {player.get('playerName', '알 수 없는 선수')}")
                producer.send('kbo-player-data', value=player)
            
            producer.flush()
            producer.close()
            logging.info("모든 선수 정보가 Kafka로 전송되었습니다.")
            
        except Exception as e:
            logging.error(f"선수 정보 스케줄링 작업 중 오류 발생: {e}")
    
    # 스케줄러 생성
    scheduler = BackgroundScheduler()
    
    # 하루에 한 번 실행 (매일 자정)
    scheduler.add_job(collect_and_send, 'cron', hour=0, minute=0, id='player_collection')
    
    # 즉시 첫 번째 실행
    collect_and_send()
    
    # 스케줄러 시작
    scheduler.start()
    logging.info("선수 정보 스케줄러가 시작되었습니다. 매일 자정에 실행됩니다.")
    
    return scheduler
