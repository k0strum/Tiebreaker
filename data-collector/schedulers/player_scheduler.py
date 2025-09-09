import logging
from apscheduler.schedulers.background import BackgroundScheduler
from collectors.player_collector import scrape_all_players_and_details
from utils.kafka_producer import create_kafka_producer
from utils.config import get_kafka_config

def schedule_player_collection():
    """
    선수 정보 데이터 수집을 스케줄링합니다.
    """
    def collect_and_send():
        """데이터를 수집하고 Kafka로 전송합니다."""
        try:
            kafka = get_kafka_config()
            # bootstrap_servers는 문자열일 수 있어 리스트로 래핑
            bs = kafka['bootstrap_servers']
            producer = create_kafka_producer(bootstrap_servers=[bs] if isinstance(bs, str) else bs)
            if not producer:
                logging.error("Kafka Producer 생성 실패")
                return
            
            # 선수 데이터 수집 및 전송
            for player in scrape_all_players_and_details():
                logging.info(f"선수 정보 전송 중: {player.get('playerName', '알 수 없는 선수')}")
                # 1) 연간(통합) 데이터 전송
                producer.send(kafka['topic_player_yearly'], value=player)

                # 2) 월간 데이터가 포함되어 있다면 별도 토픽으로 전송
                monthly_payload = {}
                has_batter_monthly = bool(player.get('batterMonthlyStats'))
                has_pitcher_monthly = bool(player.get('pitcherMonthlyStats'))
                if 'id' in player and (has_batter_monthly or has_pitcher_monthly):
                    monthly_payload['playerId'] = player['id']
                    if has_batter_monthly:
                        monthly_payload['batter'] = player['batterMonthlyStats']
                    if has_pitcher_monthly:
                        monthly_payload['pitcher'] = player['pitcherMonthlyStats']
                    producer.send(kafka['topic_player_monthly'], value=monthly_payload)
                    total_months = (len(monthly_payload.get('batter', [])) if has_batter_monthly else 0) + \
                                   (len(monthly_payload.get('pitcher', [])) if has_pitcher_monthly else 0)
                    logging.info(f"월간 전송: playerId={player['id']} months={total_months} (B:{len(monthly_payload.get('batter', [])) if has_batter_monthly else 0}, P:{len(monthly_payload.get('pitcher', [])) if has_pitcher_monthly else 0})")
            
            producer.flush()
            producer.close()
            logging.info("모든 선수 정보가 Kafka로 전송되었습니다.")
            
        except Exception as e:
            logging.error(f"선수 정보 스케줄링 작업 중 오류 발생: {e}")
    
    # 스케줄러 생성
    scheduler = BackgroundScheduler()
    
    # 하루에 한 번 실행 (매일 새벽 4시)
    scheduler.add_job(collect_and_send, 'cron', hour=4, minute=0, id='player_collection')
    
    # 즉시 첫 번째 실행
    collect_and_send()
    
    # 스케줄러 시작
    scheduler.start()
    logging.info("선수 정보 스케줄러가 시작되었습니다. 매일 새벽 4시마다 실행됩니다.")
    
    return scheduler
