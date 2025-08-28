import logging
from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.cron import CronTrigger
from collectors.live_game_simulator import LiveGameSimulator

class LiveGameSimulatorScheduler:
    """
    실시간 경기 시뮬레이터 스케줄러
    - live-game-info 토픽: 실시간 경기 상황 + 회차 변경
    - commentary 토픽: 상세 이벤트 메시지
    """
    
    def __init__(self):
        self.scheduler = BackgroundScheduler()
        self.simulator = LiveGameSimulator()
        self.is_running = False
        
    def start_simulation(self):
        """실시간 경기 시뮬레이션 시작"""
        try:
            logging.info("🎮 실시간 경기 시뮬레이션 시작")
            success = self.simulator.run_once()
            
            if success:
                logging.info("✅ 실시간 경기 시뮬레이션 완료")
            else:
                logging.error("❌ 실시간 경기 시뮬레이션 실패")
                
        except Exception as e:
            logging.error(f"❌ 실시간 경기 시뮬레이션 오류: {e}")
    
    def start(self):
        """스케줄러 시작"""
        if self.is_running:
            logging.warning("⚠️ 실시간 경기 시뮬레이터가 이미 실행 중입니다.")
            return
            
        try:
            # 매 15초마다 실시간 경기 시뮬레이션 실행
            self.scheduler.add_job(
                func=self.start_simulation,
                trigger=CronTrigger(second="*/15"),  # 15초마다
                id="live_game_simulation",
                name="실시간 경기 시뮬레이션",
                replace_existing=True
            )
            
            self.scheduler.start()
            self.is_running = True
            
            logging.info("✅ 실시간 경기 시뮬레이터 스케줄러 시작")
            logging.info("📅 스케줄: 15초마다 실시간 경기 시뮬레이션")
            
        except Exception as e:
            logging.error(f"❌ 실시간 경기 시뮬레이터 스케줄러 시작 실패: {e}")
    
    def stop(self):
        """스케줄러 중지"""
        if not self.is_running:
            logging.warning("⚠️ 실시간 경기 시뮬레이터가 실행 중이 아닙니다.")
            return
            
        try:
            self.scheduler.shutdown()
            self.is_running = False
            
            logging.info("✅ 실시간 경기 시뮬레이터 스케줄러 중지")
            
        except Exception as e:
            logging.error(f"❌ 실시간 경기 시뮬레이터 스케줄러 중지 실패: {e}")
    
    def get_status(self):
        """스케줄러 상태 반환"""
        return {
            "name": "실시간 경기 시뮬레이터",
            "is_running": self.is_running,
            "schedule": "15초마다",
            "topics": ["live-game-info", "commentary"],
            "description": "실시간 경기 상황 시뮬레이션 및 Kafka 전송"
        }

# 전역 인스턴스
live_game_simulator_scheduler = LiveGameSimulatorScheduler()
