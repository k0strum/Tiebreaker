import logging
from apscheduler.schedulers.background import BackgroundScheduler
from collectors.game_schedule_collector import collect_game_schedule, get_today_games_from_schedule
from utils.kafka_producer import create_kafka_producer

def schedule_game_schedule_collection():
    """
    경기 스케줄 데이터 수집을 스케줄링합니다.
    """
    def collect_and_send():
        """데이터를 수집하고 Kafka로 전송합니다."""
        try:
            logging.info("🔄 경기 스케줄 수집 및 전송 시작")
            collected_data = collect_game_schedule()
            
            if collected_data.get('status') == 'success':
                logging.info(f"📊 수집된 경기 수: {len(collected_data.get('data', []))}")
                
                producer = create_kafka_producer()
                if producer:
                    producer.send('kbo-game-schedule', value=collected_data)
                    producer.flush()
                    logging.info("✅ 경기 스케줄 데이터가 Kafka로 전송되었습니다.")
                    producer.close()
                else:
                    logging.error("❌ Kafka Producer 생성 실패")
            else:
                logging.error(f"❌ 경기 스케줄 데이터 수집 실패: {collected_data.get('error')}")
        except Exception as e:
            logging.error(f"❌ 경기 스케줄 스케줄링 작업 중 오류 발생: {e}")
    
    # 스케줄러 생성
    scheduler = BackgroundScheduler()
    
    # 하루에 한 번 실행 (매일 새벽 5시) - 팀 순위보다 1시간 늦게
    scheduler.add_job(collect_and_send, 'cron', hour=5, minute=0, id='game_schedule_collection')
    
    # 즉시 첫 번째 실행 (테스트용)
    collect_and_send()
    
    # 스케줄러 시작
    scheduler.start()
    logging.info("경기 스케줄 스케줄러가 시작되었습니다. 매일 새벽 5시마다 실행됩니다.")
    
    return scheduler

def schedule_today_games_collection():
    """
    오늘 경기 데이터만 수집하는 스케줄러 (더 자주 실행)
    """
    def collect_and_send_today():
        """오늘 경기 데이터를 수집하고 Kafka로 전송합니다."""
        try:
            logging.info("🔄 오늘 경기 수집 및 전송 시작")
            
            # 전체 스케줄을 먼저 수집
            schedule_data = collect_game_schedule()
            if schedule_data.get('status') != 'success':
                logging.error(f"❌ 전체 스케줄 수집 실패: {schedule_data.get('error')}")
                return
            
            # 오늘 경기만 필터링
            collected_data = get_today_games_from_schedule(schedule_data)
            
            if collected_data.get('status') == 'success':
                logging.info(f"📊 수집된 오늘 경기 수: {len(collected_data.get('data', []))}")
                
                producer = create_kafka_producer()
                if producer:
                    producer.send('kbo-today-games', value=collected_data)
                    producer.flush()
                    logging.info("✅ 오늘 경기 데이터가 Kafka로 전송되었습니다.")
                    producer.close()
                else:
                    logging.error("❌ Kafka Producer 생성 실패")
            else:
                logging.error(f"❌ 오늘 경기 데이터 필터링 실패: {collected_data.get('error')}")
        except Exception as e:
            logging.error(f"❌ 오늘 경기 스케줄링 작업 중 오류 발생: {e}")
    
    # 스케줄러 생성
    scheduler = BackgroundScheduler()
    
    # 매시간 실행 (경기 상태 업데이트를 위해)
    scheduler.add_job(collect_and_send_today, 'interval', hours=1, id='today_games_collection')
    
    # 즉시 첫 번째 실행
    collect_and_send_today()
    
    # 스케줄러 시작
    scheduler.start()
    logging.info("오늘 경기 스케줄러가 시작되었습니다. 매시간마다 실행됩니다.")
    
    return scheduler

def run_both_schedulers():
    """
    두 스케줄러를 모두 실행하는 함수
    """
    # 7일간 경기 스케줄 스케줄러 (하루 한 번)
    weekly_scheduler = schedule_game_schedule_collection()
    
    # 오늘 경기 스케줄러 (매시간)
    daily_scheduler = schedule_today_games_collection()
    
    return weekly_scheduler, daily_scheduler
