import time
import logging
import json
from datetime import datetime
from utils.kafka_producer import create_kafka_producer

class LiveGameSimulator:
    """
    실시간 경기 정보를 시뮬레이션하여 Kafka로 전송하는 클래스
    """
    
    def __init__(self):
        self.kafka_topic = 'live-game-info'
        self.producer = None
        
    def setup_kafka(self):
        """Kafka Producer 설정"""
        self.producer = create_kafka_producer()
        if not self.producer:
            logging.error("❌ Kafka Producer 생성 실패")
            return False
        return True
    
    def simulate_game_scenario(self, game_id, home_team, away_team):
        """
        특정 경기의 전체 시나리오를 시뮬레이션
        
        Args:
            game_id: 경기 ID
            home_team: 홈팀명
            away_team: 원정팀명
        """
        logging.info(f"🎮 {away_team} vs {home_team} 경기 시뮬레이션 시작")
        
        # 경기 시나리오 정의
        scenarios = [
            # 경기 시작 전
            {
                "gameId": game_id,
                "status": "READY",
                "inning": 0,
                "half": "",
                "homeScore": 0,
                "awayScore": 0,
                "homeTeam": home_team,
                "awayTeam": away_team,
                "currentBatter": "",
                "currentPitcher": "",
                "bases": [False, False, False],  # 1루, 2루, 3루
                "outs": 0,
                "balls": 0,
                "strikes": 0,
                "timestamp": int(time.time() * 1000),
                "message": f"{away_team} vs {home_team} 경기가 곧 시작됩니다."
            },
            
            # 1회초 시작
            {
                "gameId": game_id,
                "status": "LIVE",
                "inning": 1,
                "half": "T",
                "homeScore": 0,
                "awayScore": 0,
                "homeTeam": home_team,
                "awayTeam": away_team,
                "currentBatter": f"{away_team} 1번타자",
                "currentPitcher": f"{home_team} 선발투수",
                "bases": [False, False, False],
                "outs": 0,
                "balls": 0,
                "strikes": 0,
                "timestamp": int(time.time() * 1000),
                "message": f"1회초 {away_team} 공격이 시작됩니다."
            },
            
            # 1회초 진행
            {
                "gameId": game_id,
                "status": "LIVE",
                "inning": 1,
                "half": "T",
                "homeScore": 0,
                "awayScore": 1,
                "homeTeam": home_team,
                "awayTeam": away_team,
                "currentBatter": f"{away_team} 2번타자",
                "currentPitcher": f"{home_team} 선발투수",
                "bases": [True, False, False],
                "outs": 0,
                "balls": 0,
                "strikes": 0,
                "timestamp": int(time.time() * 1000),
                "message": f"1회초 {away_team} 1번타자가 안타를 쳤습니다!"
            },
            
            # 1회말 시작
            {
                "gameId": game_id,
                "status": "LIVE",
                "inning": 1,
                "half": "B",
                "homeScore": 0,
                "awayScore": 1,
                "homeTeam": home_team,
                "awayTeam": away_team,
                "currentBatter": f"{home_team} 1번타자",
                "currentPitcher": f"{away_team} 선발투수",
                "bases": [False, False, False],
                "outs": 0,
                "balls": 0,
                "strikes": 0,
                "timestamp": int(time.time() * 1000),
                "message": f"1회말 {home_team} 공격이 시작됩니다."
            },
            
            # 1회말 진행
            {
                "gameId": game_id,
                "status": "LIVE",
                "inning": 1,
                "half": "B",
                "homeScore": 2,
                "awayScore": 1,
                "homeTeam": home_team,
                "awayTeam": away_team,
                "currentBatter": f"{home_team} 2번타자",
                "currentPitcher": f"{away_team} 선발투수",
                "bases": [False, False, False],
                "outs": 0,
                "balls": 0,
                "strikes": 0,
                "timestamp": int(time.time() * 1000),
                "message": f"1회말 {home_team} 1번타자가 2점 홈런을 쳤습니다!"
            },
            
            # 2회초
            {
                "gameId": game_id,
                "status": "LIVE",
                "inning": 2,
                "half": "T",
                "homeScore": 2,
                "awayScore": 3,
                "homeTeam": home_team,
                "awayTeam": away_team,
                "currentBatter": f"{away_team} 3번타자",
                "currentPitcher": f"{home_team} 선발투수",
                "bases": [False, False, False],
                "outs": 0,
                "balls": 0,
                "strikes": 0,
                "timestamp": int(time.time() * 1000),
                "message": f"2회초 {away_team} 공격이 시작됩니다."
            },
            
            # 3회초
            {
                "gameId": game_id,
                "status": "LIVE",
                "inning": 3,
                "half": "T",
                "homeScore": 2,
                "awayScore": 4,
                "homeTeam": home_team,
                "awayTeam": away_team,
                "currentBatter": f"{away_team} 4번타자",
                "currentPitcher": f"{home_team} 선발투수",
                "bases": [False, False, False],
                "outs": 0,
                "balls": 0,
                "strikes": 0,
                "timestamp": int(time.time() * 1000),
                "message": f"3회초 {away_team} 공격이 시작됩니다."
            },
            
            # 4회초
            {
                "gameId": game_id,
                "status": "LIVE",
                "inning": 4,
                "half": "T",
                "homeScore": 2,
                "awayScore": 4,
                "homeTeam": home_team,
                "awayTeam": away_team,
                "currentBatter": f"{away_team} 5번타자",
                "currentPitcher": f"{home_team} 선발투수",
                "bases": [False, False, False],
                "outs": 0,
                "balls": 0,
                "strikes": 0,
                "timestamp": int(time.time() * 1000),
                "message": f"4회초 {away_team} 공격이 시작됩니다."
            },
            
            # 5회초
            {
                "gameId": game_id,
                "status": "LIVE",
                "inning": 5,
                "half": "T",
                "homeScore": 2,
                "awayScore": 4,
                "homeTeam": home_team,
                "awayTeam": away_team,
                "currentBatter": f"{away_team} 6번타자",
                "currentPitcher": f"{home_team} 선발투수",
                "bases": [False, False, False],
                "outs": 0,
                "balls": 0,
                "strikes": 0,
                "timestamp": int(time.time() * 1000),
                "message": f"5회초 {away_team} 공격이 시작됩니다."
            },
            
            # 6회초
            {
                "gameId": game_id,
                "status": "LIVE",
                "inning": 6,
                "half": "T",
                "homeScore": 2,
                "awayScore": 4,
                "homeTeam": home_team,
                "awayTeam": away_team,
                "currentBatter": f"{away_team} 7번타자",
                "currentPitcher": f"{home_team} 선발투수",
                "bases": [False, False, False],
                "outs": 0,
                "balls": 0,
                "strikes": 0,
                "timestamp": int(time.time() * 1000),
                "message": f"6회초 {away_team} 공격이 시작됩니다."
            },
            
            # 7회초
            {
                "gameId": game_id,
                "status": "LIVE",
                "inning": 7,
                "half": "T",
                "homeScore": 2,
                "awayScore": 4,
                "homeTeam": home_team,
                "awayTeam": away_team,
                "currentBatter": f"{away_team} 8번타자",
                "currentPitcher": f"{home_team} 선발투수",
                "bases": [False, False, False],
                "outs": 0,
                "balls": 0,
                "strikes": 0,
                "timestamp": int(time.time() * 1000),
                "message": f"7회초 {away_team} 공격이 시작됩니다."
            },
            
            # 8회초
            {
                "gameId": game_id,
                "status": "LIVE",
                "inning": 8,
                "half": "T",
                "homeScore": 2,
                "awayScore": 4,
                "homeTeam": home_team,
                "awayTeam": away_team,
                "currentBatter": f"{away_team} 9번타자",
                "currentPitcher": f"{home_team} 선발투수",
                "bases": [False, False, False],
                "outs": 0,
                "balls": 0,
                "strikes": 0,
                "timestamp": int(time.time() * 1000),
                "message": f"8회초 {away_team} 공격이 시작됩니다."
            },
            
            # 9회초
            {
                "gameId": game_id,
                "status": "LIVE",
                "inning": 9,
                "half": "T",
                "homeScore": 2,
                "awayScore": 4,
                "homeTeam": home_team,
                "awayTeam": away_team,
                "currentBatter": f"{away_team} 1번타자",
                "currentPitcher": f"{home_team} 선발투수",
                "bases": [False, False, False],
                "outs": 0,
                "balls": 0,
                "strikes": 0,
                "timestamp": int(time.time() * 1000),
                "message": f"9회초 {away_team} 공격이 시작됩니다."
            },
            
            # 경기 종료
            {
                "gameId": game_id,
                "status": "FINISHED",
                "inning": 9,
                "half": "B",
                "homeScore": 3,
                "awayScore": 4,
                "homeTeam": home_team,
                "awayTeam": away_team,
                "currentBatter": "",
                "currentPitcher": "",
                "bases": [False, False, False],
                "outs": 3,
                "balls": 0,
                "strikes": 0,
                "timestamp": int(time.time() * 1000),
                "message": f"경기가 종료되었습니다. {away_team} 4-3 {home_team}"
            }
        ]
        
        # 시나리오 실행
        for i, scenario in enumerate(scenarios):
            try:
                # Kafka로 전송
                self.producer.send(self.kafka_topic, scenario)
                self.producer.flush()
                
                logging.info(f"📡 시나리오 {i+1}/{len(scenarios)} 전송: {scenario['message']}")
                
                # 시나리오 간 대기 시간 (실제 경기처럼)
                if i == 0:  # 경기 시작 전
                    time.sleep(10)  # 10초 대기
                elif scenario['status'] == 'FINISHED':  # 경기 종료
                    time.sleep(5)   # 5초 대기
                else:  # 경기 진행 중
                    time.sleep(15)  # 15초마다 업데이트
                    
            except Exception as e:
                logging.error(f"❌ 시나리오 {i+1} 전송 실패: {e}")
    
    def simulate_multiple_games(self):
        """
        여러 경기를 동시에 시뮬레이션
        """
        games = [
            {
                "gameId": "20250827HHWO02025",
                "homeTeam": "한화",
                "awayTeam": "키움"
            },
            {
                "gameId": "20250827LGLT02025", 
                "homeTeam": "LG",
                "awayTeam": "롯데"
            },
            {
                "gameId": "20250827SSNC02025",
                "homeTeam": "삼성", 
                "awayTeam": "NC"
            }
        ]
        
        logging.info(f"🎮 {len(games)}개 경기 시뮬레이션 시작")
        
        for game in games:
            # 각 경기를 별도 스레드로 실행 (간단한 시뮬레이션을 위해 순차 실행)
            self.simulate_game_scenario(
                game["gameId"], 
                game["homeTeam"], 
                game["awayTeam"]
            )
    
    def run_once(self):
        """
        한 번의 시뮬레이션 실행
        """
        if not self.setup_kafka():
            return False
            
        try:
            self.simulate_multiple_games()
            logging.info("✅ 실시간 경기 시뮬레이션 완료")
            return True
        except Exception as e:
            logging.error(f"❌ 시뮬레이션 실행 중 오류: {e}")
            return False
        finally:
            if self.producer:
                self.producer.close()

def test_live_game_simulator():
    """
    실시간 경기 시뮬레이터 테스트 함수
    """
    print("=" * 60)
    print("🎮 실시간 경기 시뮬레이터 테스트 시작")
    print("=" * 60)
    
    simulator = LiveGameSimulator()
    success = simulator.run_once()
    
    if success:
        print("✅ 시뮬레이션 성공!")
    else:
        print("❌ 시뮬레이션 실패!")
    
    print("=" * 60)

if __name__ == "__main__":
    # 로깅 설정
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(levelname)s - %(message)s'
    )
    
    # 테스트 실행
    test_live_game_simulator()
