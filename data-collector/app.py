from flask import Flask, jsonify
import logging
import atexit
from utils.kafka_producer import create_kafka_producer
from schedulers.team_rank_scheduler import schedule_team_rank_collection
from schedulers.player_scheduler import schedule_player_collection
from api.team_rank_api import team_rank_bp
from api.player_api import player_bp

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

# Flask 애플리케이션 인스턴스 생성
app = Flask(__name__)

# Blueprint 등록
app.register_blueprint(team_rank_bp)
app.register_blueprint(player_bp)

# 스케줄러들
team_rank_scheduler = None
player_scheduler = None

def initialize_schedulers():
    """스케줄러들을 초기화합니다."""
    global team_rank_scheduler, player_scheduler
    
    try:
        # Kafka Producer 연결 테스트
        producer = create_kafka_producer()
        if producer:
            producer.close()
            logging.info("Kafka 연결 테스트 성공")
            
            # 스케줄러 시작
            team_rank_scheduler = schedule_team_rank_collection()
            player_scheduler = schedule_player_collection()
            
            logging.info("모든 스케줄러가 성공적으로 시작되었습니다.")
        else:
            logging.critical("Kafka 연결 실패로 스케줄러를 시작할 수 없습니다.")
            
    except Exception as e:
        logging.error(f"스케줄러 초기화 중 오류 발생: {e}")

# 루트 경로에 대한 라우트 핸들러 정의
@app.route('/')
def home():
    return "Data Collector Server is running!"

# 상태 확인 엔드포인트
@app.route('/health')
def health():
    return jsonify({"status": "ok"})

# 스케줄러 상태 확인 엔드포인트
@app.route('/api/schedulers/status')
def scheduler_status():
    return jsonify({
        "team_rank_scheduler": "running" if team_rank_scheduler else "stopped",
        "player_scheduler": "running" if player_scheduler else "stopped"
    })

# 스케줄러 초기화를 위한 전역 변수
_schedulers_initialized = False

# 앱 시작 시 스케줄러 초기화 (한 번만 실행되도록)
def initialize_schedulers_once():
    global _schedulers_initialized, team_rank_scheduler, player_scheduler
    
    if _schedulers_initialized:
        return
    
    try:
        # Kafka Producer 연결 테스트
        producer = create_kafka_producer()
        if producer:
            producer.close()
            logging.info("Kafka 연결 테스트 성공")
            
            # 스케줄러 시작
            team_rank_scheduler = schedule_team_rank_collection()
            player_scheduler = schedule_player_collection()
            
            _schedulers_initialized = True
            logging.info("모든 스케줄러가 성공적으로 시작되었습니다.")
        else:
            logging.critical("Kafka 연결 실패로 스케줄러를 시작할 수 없습니다.")
            
    except Exception as e:
        logging.error(f"스케줄러 초기화 중 오류 발생: {e}")

# 앱 종료 시 스케줄러 정리
def cleanup_schedulers():
    global team_rank_scheduler, player_scheduler
    if team_rank_scheduler:
        team_rank_scheduler.shutdown()
        logging.info("팀 랭킹 스케줄러가 종료되었습니다.")
    if player_scheduler:
        player_scheduler.shutdown()
        logging.info("선수 정보 스케줄러가 종료되었습니다.")

atexit.register(cleanup_schedulers)

# 이 파일이 "직접" python app.py로 실행될 때만 Flask 개발 서버를 구동합니다.
if __name__ == '__main__':
    # 개발 모드에서는 즉시 스케줄러 초기화
    initialize_schedulers_once()
    app.run(host='0.0.0.0', port=5000, debug=False, use_reloader=False)
else:
    # gunicorn으로 실행될 때는 첫 번째 요청 시 스케줄러 초기화
    initialize_schedulers_once()