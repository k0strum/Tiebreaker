import time
import logging
import json
from datetime import datetime
from utils.kafka_producer import create_kafka_producer
import threading

class LiveGameSimulator:
    """
    실시간 경기 정보를 시뮬레이션하여 Kafka로 전송하는 클래스
    - live-game-info 토픽: 실시간 경기 상황 + 회차 변경
    - commentary 토픽: 상세 이벤트 메시지 (기존 시스템과 연동)
    """
    
    def __init__(self):
        self.live_game_topic = 'live-game-info'
        self.commentary_topic = 'commentary'
        self.producer = None
        
    def setup_kafka(self):
        """Kafka Producer 설정"""
        self.producer = create_kafka_producer()
        if not self.producer:
            logging.error("❌ Kafka Producer 생성 실패")
            return False
        logging.info(f"✅ Kafka Producer 설정 완료")
        logging.info(f"   - live-game-info 토픽: {self.live_game_topic}")
        logging.info(f"   - commentary 토픽: {self.commentary_topic}")
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
        
        # 경기 시나리오 정의 (최적화된 구조)
        scenarios = [
            # 경기 시작 전
            {
                "live_game": {
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
                    "timestamp": int(time.time() * 1000)
                },
                "commentary": {
                    "gameId": game_id,
                    "ts": int(time.time() * 1000),
                    "text": f"{away_team} vs {home_team} 경기가 곧 시작됩니다.",
                    "severity": "INFO",
                    "inning": 0,
                    "half": ""
                }
            },
            
            # 1회초 시작
            {
                "live_game": {
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
                    "timestamp": int(time.time() * 1000)
                },
                "commentary": {
                    "gameId": game_id,
                    "ts": int(time.time() * 1000),
                    "text": f"1회초 {away_team} 공격이 시작됩니다.",
                    "severity": "INFO",
                    "inning": 1,
                    "half": "T"
                }
            },
            
            # 1회초 진행 (안타)
            {
                "live_game": {
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
                    "timestamp": int(time.time() * 1000)
                },
                "commentary": {
                    "gameId": game_id,
                    "ts": int(time.time() * 1000),
                    "text": f"1회초 {away_team} 1번타자가 안타를 쳤습니다!",
                    "severity": "INFO",
                    "inning": 1,
                    "half": "T"
                }
            },
            
            # 1회말 시작
            {
                "live_game": {
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
                    "timestamp": int(time.time() * 1000)
                },
                "commentary": {
                    "gameId": game_id,
                    "ts": int(time.time() * 1000),
                    "text": f"1회말 {home_team} 공격이 시작됩니다.",
                    "severity": "INFO",
                    "inning": 1,
                    "half": "B"
                }
            },
            
            # 1회말 진행 (홈런)
            {
                "live_game": {
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
                    "timestamp": int(time.time() * 1000)
                },
                "commentary": {
                    "gameId": game_id,
                    "ts": int(time.time() * 1000),
                    "text": f"1회말 {home_team} 1번타자가 2점 홈런을 쳤습니다!",
                    "severity": "INFO",
                    "inning": 1,
                    "half": "B"
                }
            },
            
            # 2회초 시작 (회차 변경)
            {
                "live_game": {
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
                    "timestamp": int(time.time() * 1000)
                },
                "commentary": {
                    "gameId": game_id,
                    "ts": int(time.time() * 1000),
                    "text": f"2회초 {away_team} 공격이 시작됩니다.",
                    "severity": "INFO",
                    "inning": 2,
                    "half": "T"
                }
            },
            
            # 3회초 시작 (회차 변경)
            {
                "live_game": {
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
                    "timestamp": int(time.time() * 1000)
                },
                "commentary": {
                    "gameId": game_id,
                    "ts": int(time.time() * 1000),
                    "text": f"3회초 {away_team} 공격이 시작됩니다.",
                    "severity": "INFO",
                    "inning": 3,
                    "half": "T"
                }
            },
            
            # 4회초 시작 (회차 변경)
            {
                "live_game": {
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
                    "timestamp": int(time.time() * 1000)
                },
                "commentary": {
                    "gameId": game_id,
                    "ts": int(time.time() * 1000),
                    "text": f"4회초 {away_team} 공격이 시작됩니다.",
                    "severity": "INFO",
                    "inning": 4,
                    "half": "T"
                }
            },
            
            # 5회초 시작 (회차 변경)
            {
                "live_game": {
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
                    "timestamp": int(time.time() * 1000)
                },
                "commentary": {
                    "gameId": game_id,
                    "ts": int(time.time() * 1000),
                    "text": f"5회초 {away_team} 공격이 시작됩니다.",
                    "severity": "INFO",
                    "inning": 5,
                    "half": "T"
                }
            },
            
            # 6회초 시작 (회차 변경)
            {
                "live_game": {
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
                    "timestamp": int(time.time() * 1000)
                },
                "commentary": {
                    "gameId": game_id,
                    "ts": int(time.time() * 1000),
                    "text": f"6회초 {away_team} 공격이 시작됩니다.",
                    "severity": "INFO",
                    "inning": 6,
                    "half": "T"
                }
            },
            
            # 7회초 시작 (회차 변경)
            {
                "live_game": {
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
                    "timestamp": int(time.time() * 1000)
                },
                "commentary": {
                    "gameId": game_id,
                    "ts": int(time.time() * 1000),
                    "text": f"7회초 {away_team} 공격이 시작됩니다.",
                    "severity": "INFO",
                    "inning": 7,
                    "half": "T"
                }
            },
            
            # 8회초 시작 (회차 변경)
            {
                "live_game": {
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
                    "timestamp": int(time.time() * 1000)
                },
                "commentary": {
                    "gameId": game_id,
                    "ts": int(time.time() * 1000),
                    "text": f"8회초 {away_team} 공격이 시작됩니다.",
                    "severity": "INFO",
                    "inning": 8,
                    "half": "T"
                }
            },
            
            # 9회초 시작 (회차 변경)
            {
                "live_game": {
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
                    "timestamp": int(time.time() * 1000)
                },
                "commentary": {
                    "gameId": game_id,
                    "ts": int(time.time() * 1000),
                    "text": f"9회초 {away_team} 공격이 시작됩니다.",
                    "severity": "INFO",
                    "inning": 9,
                    "half": "T"
                }
            },
            
            # 경기 종료
            {
                "live_game": {
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
                    "timestamp": int(time.time() * 1000)
                },
                "commentary": {
                    "gameId": game_id,
                    "ts": int(time.time() * 1000),
                    "text": f"경기가 종료되었습니다. {away_team} 4-3 {home_team} 승리!",
                    "severity": "INFO",
                    "inning": 9,
                    "half": "B"
                }
            }
        ]
        
        # 시나리오 실행
        for i, scenario in enumerate(scenarios):
            try:
                # live-game-info 토픽으로 실시간 경기 정보 전송
                live_game_data = scenario["live_game"]
                self.producer.send(self.live_game_topic, live_game_data)
                
                # commentary 토픽으로 상세 이벤트 메시지 전송
                commentary_data = scenario["commentary"]
                self.producer.send(self.commentary_topic, commentary_data)
                
                self.producer.flush()
                
                logging.info(f"📡 시나리오 {i+1}/{len(scenarios)} 전송:")
                logging.info(f"   - live-game-info: {live_game_data['status']} {live_game_data['inning']}회{live_game_data['half']}")
                logging.info(f"   - commentary: {commentary_data['text']}")
                
                # 시나리오 간 대기 시간 (실제 경기처럼)
                if i == 0:  # 경기 시작 전
                    time.sleep(10)  # 10초 대기
                elif live_game_data['status'] == 'FINISHED':  # 경기 종료
                    time.sleep(5)   # 5초 대기
                else:  # 경기 진행 중
                    time.sleep(15)  # 15초마다 업데이트
                    
            except Exception as e:
                logging.error(f"❌ 시나리오 {i+1} 전송 실패: {e}")
    
    def simulate_multiple_games(self):
        """
        여러 경기를 동시에 시뮬레이션 (진짜 동시 실행)
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
            },
            {
                "gameId": "20250827KTSK02025",
                "homeTeam": "KT",
                "awayTeam": "SK"
            },
            {
                "gameId": "20250827DOOS02025",
                "homeTeam": "두산",
                "awayTeam": "오리온"
            }
        ]
        
        logging.info(f"🎮 {len(games)}개 경기 동시 시뮬레이션 시작")
        
        # 각 경기를 별도 스레드로 실행 (진짜 동시 실행)
        threads = []
        for i, game in enumerate(games):
            # 각 경기마다 시작 시간을 다르게 설정 (실제 경기처럼)
            start_delay = i * 30  # 30초씩 차이로 시작
            
            thread = threading.Thread(
                target=self.simulate_game_scenario_continuous,
                args=(game["gameId"], game["homeTeam"], game["awayTeam"], start_delay),
                daemon=True
            )
            threads.append(thread)
            thread.start()
            
            logging.info(f"   - {game['awayTeam']} vs {game['homeTeam']} 스레드 시작 (지연: {start_delay}초)")
        
        # 스레드들이 계속 실행되도록 대기 (join 제거)
        logging.info("✅ 모든 경기 스레드가 백그라운드에서 실행 중...")
        logging.info("   (각 경기는 15초마다 업데이트되며 계속 실행됩니다)")
        
        # 메인 스레드는 계속 실행 상태 유지
        try:
            while True:
                time.sleep(60)  # 1분마다 상태 로그
                logging.info("🔄 5경기 시뮬레이션 계속 실행 중...")
        except KeyboardInterrupt:
            logging.info("⏹️ 시뮬레이션 중단 요청됨")
    
    def simulate_game_scenario_continuous(self, game_id, home_team, away_team, start_delay=0):
        """
        개별 경기를 계속 시뮬레이션하는 스레드 함수 (무한 루프)
        """
        try:
            # 시작 지연
            if start_delay > 0:
                logging.info(f"⏰ {away_team} vs {home_team} 경기 {start_delay}초 후 시작")
                time.sleep(start_delay)
            
            # 경기 상태 초기화
            current_inning = 0
            current_half = ""
            home_score = 0
            away_score = 0
            status = "READY"
            
            logging.info(f"🎮 {away_team} vs {home_team} 경기 시작!")
            
            # 무한 루프로 계속 시뮬레이션
            while True:
                try:
                    # 경기 진행 상황 업데이트
                    if status == "READY":
                        # 경기 시작
                        status = "LIVE"
                        current_inning = 1
                        current_half = "T"
                        logging.info(f"🚀 {away_team} vs {home_team} 경기 시작! 1회초")
                    
                    elif status == "LIVE":
                        # 회차 진행
                        if current_half == "T":
                            current_half = "B"
                            logging.info(f"🔄 {away_team} vs {home_team} {current_inning}회말")
                        else:
                            current_inning += 1
                            current_half = "T"
                            logging.info(f"🔄 {away_team} vs {home_team} {current_inning}회초")
                        
                        # 점수 변화 (랜덤)
                        if current_inning <= 3:
                            # 초반에는 점수 변화 적음
                            if current_inning % 2 == 0:
                                away_score += 1
                        elif current_inning <= 6:
                            # 중반에는 점수 변화 증가
                            if current_inning % 2 == 0:
                                home_score += 1
                        else:
                            # 후반에는 점수 변화 많음
                            if current_inning % 2 == 0:
                                away_score += 1
                            else:
                                home_score += 1
                        
                        # 9회말 이후 종료
                        if current_inning > 9 and current_half == "B":
                            status = "FINISHED"
                            logging.info(f"🏁 {away_team} vs {home_team} 경기 종료! 최종 스코어: {away_team} {away_score}-{home_score} {home_team}")
                    
                    # 실시간 경기 데이터 생성
                    live_game_data = {
                        "gameId": game_id,
                        "status": status,
                        "inning": current_inning,
                        "half": current_half,
                        "homeScore": home_score,
                        "awayScore": away_score,
                        "homeTeam": home_team,
                        "awayTeam": away_team,
                        "currentBatter": f"{away_team if current_half == 'T' else home_team} {current_inning}번타자" if status == "LIVE" else "",
                        "currentPitcher": f"{home_team if current_half == 'T' else away_team} 선발투수" if status == "LIVE" else "",
                        "bases": [False, False, False],  # 베이스 상황 (간단화)
                        "outs": 0 if status == "LIVE" else 3,
                        "balls": 0,
                        "strikes": 0,
                        "timestamp": int(time.time() * 1000)
                    }
                    
                    # 해설 데이터 생성
                    commentary_data = {
                        "gameId": game_id,
                        "ts": int(time.time() * 1000),
                        "text": f"{current_inning}회{current_half} {away_team if current_half == 'T' else home_team} 공격이 진행 중입니다." if status == "LIVE" else f"{away_team} vs {home_team} 경기가 종료되었습니다.",
                        "severity": "INFO",
                        "inning": current_inning,
                        "half": current_half
                    }
                    
                    # Kafka로 전송 (메인 틱: 15초)
                    self.producer.send(self.live_game_topic, live_game_data)
                    self.producer.send(self.commentary_topic, commentary_data)
                    self.producer.flush()

                    logging.info(f"📡 {away_team} vs {home_team}: {status} {current_inning}회{current_half} ({away_score}-{home_score})")

                    # 중간 commentary 추가 전송: 5초, 10초 지점에서만 commentary 전송
                    for mid_idx in range(2):
                        time.sleep(5)
                        if status != "LIVE":
                            continue
                        mid_commentary = {
                            "gameId": game_id,
                            "ts": int(time.time() * 1000),
                            "text": f"{current_inning}회{current_half} 진행 중... 상황 업데이트",
                            "severity": "INFO",
                            "inning": current_inning,
                            "half": current_half
                        }
                        try:
                            self.producer.send(self.commentary_topic, mid_commentary)
                            self.producer.flush()
                            logging.info(f"📝 중간 해설 전송: {mid_commentary['text']}")
                        except Exception as e:
                            logging.error(f"❌ 중간 해설 전송 실패: {e}")

                    # 마지막 5초 대기 (총 15초 주기 유지)
                    time.sleep(5)
                    
                    # 경기가 종료되면 새로운 경기 시작
                    if status == "FINISHED":
                        time.sleep(30)  # 30초 대기 후 새 경기
                        status = "READY"
                        current_inning = 0
                        current_half = ""
                        home_score = 0
                        away_score = 0
                        logging.info(f"🔄 {away_team} vs {home_team} 새 경기 준비 중...")
                    
                except Exception as e:
                    logging.error(f"❌ {away_team} vs {home_team} 경기 업데이트 오류: {e}")
                    time.sleep(15)  # 오류 시에도 15초 대기
                    
        except Exception as e:
            logging.error(f"❌ {away_team} vs {home_team} 경기 스레드 오류: {e}")
    
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
        print("📊 전송된 데이터:")
        print("   - live-game-info: 실시간 경기 상황 + 회차 변경")
        print("   - commentary: 상세 이벤트 메시지")
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
