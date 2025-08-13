import logging
from apscheduler.schedulers.background import BackgroundScheduler
from collectors.team_rank_collector import collect_kbo_data
from utils.kafka_producer import create_kafka_producer

def schedule_team_rank_collection():
    """
    팀 랭킹 데이터 수집을 스케줄링합니다.
    """
    def collect_and_send():
        """데이터를 수집하고 Kafka로 전송합니다."""
        try:
            collected_data = collect_kbo_data()
            
            if collected_data.get('status') == 'success':
                producer = create_kafka_producer()
                if producer:
                    producer.send('kbo-rank-data', value=collected_data)
                    producer.flush()
                    logging.info("팀 랭킹 데이터가 Kafka로 전송되었습니다.")
                    producer.close()
                else:
                    logging.error("Kafka Producer 생성 실패")
            else:
                logging.error(f"팀 랭킹 데이터 수집 실패: {collected_data.get('error')}")
        except Exception as e:
            logging.error(f"팀 랭킹 스케줄링 작업 중 오류 발생: {e}")
    
    # 스케줄러 생성
    scheduler = BackgroundScheduler()
    
    # 1시간마다 실행
    scheduler.add_job(collect_and_send, 'interval', hours=1, id='team_rank_collection')
    
    # 즉시 첫 번째 실행
    collect_and_send()
    
    # 스케줄러 시작
    scheduler.start()
    logging.info("팀 랭킹 스케줄러가 시작되었습니다. 1시간 간격으로 실행됩니다.")
    
    return scheduler
