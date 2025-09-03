from flask import Flask, jsonify, request
from flask_cors import CORS
import logging
import atexit
import threading
from utils.kafka_producer import create_kafka_producer
from schedulers.team_rank_scheduler import schedule_team_rank_collection
from schedulers.player_scheduler import schedule_player_collection
from schedulers.game_schedule_scheduler import run_both_schedulers
from schedulers.live_game_simulator_scheduler import live_game_simulator_scheduler
from collectors.live_game_simulator import LiveGameSimulator
from api.team_rank_api import team_rank_bp
from api.player_api import player_bp

# 로깅 설정
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

# Flask 애플리케이션 인스턴스 생성
app = Flask(__name__)
# CORS 설정: Vite 개발서버에서 오는 요청 허용
CORS(app, resources={r"/api/*": {"origins": ["http://localhost:5173"]}})

# Blueprint 등록
app.register_blueprint(team_rank_bp)
app.register_blueprint(player_bp)

# 스케줄러들
team_rank_scheduler = None
player_scheduler = None
game_schedule_weekly_scheduler = None
game_schedule_daily_scheduler = None
live_game_simulator_scheduler_instance = None

# 온디맨드 시뮬레이터 실행 상태
_simulator_thread = None
_simulator_running = False
_simulator_lock = threading.Lock()

def initialize_schedulers():
    """스케줄러들을 초기화합니다."""
    global team_rank_scheduler, player_scheduler, game_schedule_weekly_scheduler, game_schedule_daily_scheduler
    
    try:
        # Kafka Producer 연결 테스트
        producer = create_kafka_producer()
        if producer:
            producer.close()
            logging.info("Kafka 연결 테스트 성공")
            
            # 스케줄러 시작
            team_rank_scheduler = schedule_team_rank_collection()
            player_scheduler = schedule_player_collection()
            game_schedule_weekly_scheduler, game_schedule_daily_scheduler = run_both_schedulers()
            
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
        "player_scheduler": "running" if player_scheduler else "stopped",
        "game_schedule_weekly_scheduler": "running" if game_schedule_weekly_scheduler else "stopped",
        "game_schedule_daily_scheduler": "running" if game_schedule_daily_scheduler else "stopped",
        "live_game_simulator_scheduler": "running" if live_game_simulator_scheduler_instance else "stopped",
        "description": {
            "team_rank_scheduler": "팀 랭킹 수집 (매일 새벽 4시)",
            "player_scheduler": "선수 정보 수집 (매일 새벽 3시)",
            "game_schedule_weekly_scheduler": "연간 경기 스케줄 수집 (매일 새벽 5시)",
            "game_schedule_daily_scheduler": "오늘 경기 실시간 수집 (1분마다)",
            "live_game_simulator_scheduler": "실시간 경기 시뮬레이션"
        }
    })

# 스케줄러 초기화를 위한 전역 변수
_schedulers_initialized = False

# 앱 시작 시 스케줄러 초기화 (한 번만 실행되도록)
def initialize_schedulers_once():
    global _schedulers_initialized, team_rank_scheduler, player_scheduler, game_schedule_weekly_scheduler, game_schedule_daily_scheduler, live_game_simulator_scheduler_instance
    
    if _schedulers_initialized:
        return
    
    try:
        # Kafka Producer 연결 테스트
        producer = create_kafka_producer()
        if producer:
            producer.close()
            logging.info("Kafka 연결 테스트 성공")
            
            # 스케줄러 시작
            team_rank_scheduler = schedule_team_rank_collection() # 팀 랭킹
            player_scheduler = schedule_player_collection() # 선수 데이터
            game_schedule_weekly_scheduler, game_schedule_daily_scheduler = run_both_schedulers() # 경기 스케줄
            # 실시간 경기 시뮬레이션: 초회 1회만 실행
            try:
                simulator = LiveGameSimulator()
                simulator.run_once()
                logging.info("실시간 경기 시뮬레이션을 초기 실행 완료")
            except Exception as e:
                logging.error(f"초기 실시간 경기 시뮬레이션 실행 실패: {e}")
            
            _schedulers_initialized = True
            logging.info("모든 스케줄러가 성공적으로 시작되었습니다.")
        else:
            logging.critical("Kafka 연결 실패로 스케줄러를 시작할 수 없습니다.")
            
    except Exception as e:
        logging.error(f"스케줄러 초기화 중 오류 발생: {e}")

# 앱 종료 시 스케줄러 정리
def cleanup_schedulers():
    global team_rank_scheduler, player_scheduler, game_schedule_weekly_scheduler, game_schedule_daily_scheduler
    if team_rank_scheduler:
        team_rank_scheduler.shutdown()
        logging.info("팀 랭킹 스케줄러가 종료되었습니다.")
    if player_scheduler:
        player_scheduler.shutdown()
        logging.info("선수 정보 스케줄러가 종료되었습니다.")
    if game_schedule_weekly_scheduler:
        game_schedule_weekly_scheduler.shutdown()
        logging.info("경기 스케줄 주간 스케줄러가 종료되었습니다.")
    if game_schedule_daily_scheduler:
        game_schedule_daily_scheduler.shutdown()
        logging.info("경기 스케줄 일간 스케줄러가 종료되었습니다.")


# =========================
# On-demand Simulator API
# =========================

def _run_simulator_background():
    global _simulator_running
    try:
        logging.info("[API] 온디맨드 시뮬레이터 실행 시작")
        simulator = LiveGameSimulator()
        simulator.run_once()
        logging.info("[API] 온디맨드 시뮬레이터 실행 완료")
    except Exception as e:
        logging.error(f"[API] 온디맨드 시뮬레이터 실행 실패: {e}")
    finally:
        with _simulator_lock:
            _simulator_running = False


@app.route('/api/simulator/start', methods=['POST'])
def api_start_simulator():
    """온디맨드로 실시간 경기 시뮬레이터 1회 실행 (비동기)."""
    global _simulator_thread, _simulator_running
    with _simulator_lock:
        if _simulator_running:
            return jsonify({
                "status": "busy",
                "message": "시뮬레이터가 이미 실행 중입니다."
            }), 409
        _simulator_running = True
        _simulator_thread = threading.Thread(target=_run_simulator_background, daemon=True)
        _simulator_thread.start()
        return jsonify({
            "status": "started",
            "message": "시뮬레이터 실행을 시작했습니다.",
        })


@app.route('/api/simulator/status', methods=['GET'])
def api_simulator_status():
    """온디맨드 시뮬레이터 실행 상태 조회."""
    running = False
    with _simulator_lock:
        running = _simulator_running
    return jsonify({
        "running": running
    })

atexit.register(cleanup_schedulers)

# Flask 개발 서버 실행
if __name__ == '__main__':
    # 스케줄러 초기화
    initialize_schedulers_once()
    # Flask 서버 시작
    app.run(host='0.0.0.0', port=5000, debug=False, use_reloader=False)