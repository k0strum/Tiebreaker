import time
import logging
import json
from datetime import datetime
from dataclasses import dataclass, asdict
from typing import List, Optional
from utils.kafka_producer import create_kafka_producer
import threading
import random

@dataclass
class LiveGameData:
    """실시간 경기 데이터 구조"""
    gameId: str
    status: str  # READY, LIVE, FINISHED
    inning: int
    half: str    # T (초), B (말)
    homeScore: int
    awayScore: int
    homeTeam: str
    awayTeam: str
    currentBatter: str
    currentPitcher: str
    bases: List[bool]  # [1루, 2루, 3루]
    outs: int
    balls: int
    strikes: int
    timestamp: int

@dataclass
class CommentaryData:
    """해설 데이터 구조 (CommentaryEvent와 일치)"""
    gameId: str
    ts: int
    text: str
    severity: str  # INFO, WARNING, ERROR (CommentaryEvent와 매칭)
    inning: int
    half: str
    eventType: str  # HIT, OUT, RUN, INNING_CHANGE, GAME_START, GAME_END, EXTRA_INNING
    playerName: Optional[str] = None
    teamName: Optional[str] = None

class LiveGameSimulator:
    """
    실제 야구경기처럼 시뮬레이션하는 클래스
    - 5개 경기가 동시에 시작
    - 4경기는 9회에 정규 종료
    - 1경기는 연장전으로 11회까지 진행
    """
    
    def __init__(self):
        self.live_game_topic = 'live-game-info'
        self.commentary_topic = 'livegame'  # Java Consumer와 토픽명 통일
        self.producer = None
        self.games = [
            {
                "gameId": "20250827HHWO02025",
                "homeTeam": "한화",
                "awayTeam": "키움",
                "maxInnings": 9  # 정규 종료
            },
            {
                "gameId": "20250827LGLT02025", 
                "homeTeam": "LG",
                "awayTeam": "롯데",
                "maxInnings": 9  # 정규 종료
            },
            {
                "gameId": "20250827SSNC02025",
                "homeTeam": "삼성", 
                "awayTeam": "NC",
                "maxInnings": 9  # 정규 종료
            },
            {
                "gameId": "20250827KTSK02025",
                "homeTeam": "KT",
                "awayTeam": "SSG",
                "maxInnings": 9  # 정규 종료
            },
            {
                "gameId": "20250827DOOS02025",
                "homeTeam": "두산",
                "awayTeam": "KIA",
                "maxInnings": 11  # 연장전
            }
        ]
        
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
    
    def _get_current_batter(self, team_name: str, inning: int, batter_order: int) -> str:
        """현재 타자 정보 생성"""
        return f"{team_name} {batter_order}번타자"
    
    def _get_current_pitcher(self, team_name: str) -> str:
        """현재 투수 정보 생성"""
        return f"{team_name} 선발투수"
    
    def _simulate_at_bat(self) -> dict:
        """타석 시뮬레이션 (안타, 아웃, 홈런 등)"""
        # 야구에 더 가까운 낮은 득점 확률로 이벤트 생성
        # 대략적인 목표 분포: OUT ~70%, WALK ~10%, 1B ~12%, 2B ~5%, 3B ~1%, HR ~2%
        rand = random.random()

        if rand < 0.12:  # 12% 단일 안타
            return {
                "type": "HIT",
                "text": "안타를 쳤습니다!",
                "bases": [True, False, False],
                "runs": 0
            }
        elif rand < 0.17:  # +5% 2루타
            return {
                "type": "DOUBLE",
                "text": "2루타를 쳤습니다!",
                "bases": [False, True, False],
                "runs": 0
            }
        elif rand < 0.18:  # +1% 3루타
            return {
                "type": "TRIPLE",
                "text": "3루타를 쳤습니다!",
                "bases": [False, False, True],
                "runs": 0
            }
        elif rand < 0.20:  # +2% 홈런
            return {
                "type": "HOMERUN",
                "text": "홈런을 쳤습니다!",
                    "bases": [False, False, False],
                "runs": 1
            }
        elif rand < 0.30:  # +10% 볼넷
            return {
                "type": "WALK",
                "text": "볼넷으로 출루했습니다.",
                    "bases": [True, False, False],
                "runs": 0
            }
        else:  # 70% 아웃
            return {
                "type": "OUT",
                "text": "아웃되었습니다.",
                    "bases": [False, False, False],
                "runs": 0
            }
    
    def _simulate_inning(self, game_id: str, home_team: str, away_team: str, 
                         inning: int, half: str, current_score: dict) -> dict:
        """이닝 시뮬레이션"""
        at_bats = []
        outs = 0
        bases = [False, False, False]  # [1루, 2루, 3루]
        runs = 0
        
        # 3아웃까지 타석 진행
        while outs < 3:
            at_bat = self._simulate_at_bat()
            at_bats.append(at_bat)
            
            if at_bat["type"] == "OUT":
                outs += 1
            else:
                # 베이스 러닝 시뮬레이션
                if at_bat["bases"][0]:  # 1루 진루
                    if bases[0]:  # 1루에 주자가 있으면
                        if bases[1]:  # 2루에도 주자가 있으면
                            if bases[2]:  # 3루에도 주자가 있으면
                                runs += 1  # 3루 주자 홈인
                            bases[2] = True  # 2루 주자 3루로
                        bases[1] = True  # 1루 주자 2루로
                    bases[0] = True  # 타자 1루로
                
                if at_bat["bases"][1]:  # 2루 진루
                    if bases[0]:  # 1루에 주자가 있으면
                        if bases[1]:  # 2루에도 주자가 있으면
                            if bases[2]:  # 3루에도 주자가 있으면
                                runs += 1  # 3루 주자 홈인
                        bases[2] = True  # 2루 주자 3루로
                    bases[1] = True  # 타자 2루로
                
                if at_bat["bases"][2]:  # 3루 진루
                    if bases[0]:  # 1루에 주자가 있으면
                        if bases[1]:  # 2루에도 주자가 있으면
                            if bases[2]:  # 3루에도 주자가 있으면
                                runs += 1  # 3루 주자 홈인
                    bases[2] = True  # 타자 3루로
                
                # 홈런인 경우 베이스 클리어
                if at_bat["type"] == "HOMERUN":
                    runs += sum(bases) + 1  # 베이스 주자들 + 타자
                    bases = [False, False, False]
                
                # 추가 득점
                runs += at_bat["runs"]
        
        # 점수 업데이트
        if half == "T":  # 원정팀 공격
            current_score["awayScore"] += runs
        else:  # 홈팀 공격
            current_score["homeScore"] += runs
        
        return {
            "at_bats": at_bats,
            "outs": outs,
            "bases": bases,
            "runs": runs,
            "final_score": current_score.copy()
        }
    
    def create_live_game_data(self, game_id: str, home_team: str, away_team: str, 
                             status: str, inning: int, half: str, 
                             home_score: int, away_score: int, bases: List[bool], outs: int) -> LiveGameData:
        """표준화된 실시간 경기 데이터 생성"""
        return LiveGameData(
            gameId=game_id,
            status=status,
            inning=inning,
            half=half,
            homeScore=home_score,
            awayScore=away_score,
            homeTeam=home_team,
            awayTeam=away_team,
            currentBatter=self._get_current_batter(away_team if half == 'T' else home_team, inning, random.randint(1, 9)),
            currentPitcher=self._get_current_pitcher(home_team if half == 'T' else away_team),
            bases=bases,
            outs=outs,
            balls=random.randint(0, 3),
            strikes=random.randint(0, 2),
            timestamp=int(time.time() * 1000)
        )
    
    def create_commentary_data(self, game_id: str, text: str, 
                              inning: int, half: str, event_type: str,
                              player_name: str = None, team_name: str = None) -> CommentaryData:
        """표준화된 해설 데이터 생성 (CommentaryEvent 형식에 맞춤)"""
        return CommentaryData(
            gameId=game_id,
            ts=int(time.time() * 1000),
            text=text,
            severity="INFO",  # CommentaryEvent의 severity 필드와 매칭
            inning=inning,
            half=half,
            eventType=event_type,
            playerName=player_name,
            teamName=team_name
        )
    
    def send_to_kafka(self, live_game_data: LiveGameData, commentary_data: CommentaryData):
        """Kafka로 데이터 전송"""
        try:
            # LiveGameData를 dict로 변환하여 전송
            live_game_dict = asdict(live_game_data)
            self.producer.send(self.live_game_topic, live_game_dict)
            
            # CommentaryData를 CommentaryEvent 형식에 맞게 변환하여 전송
            commentary_event = {
                'gameId': commentary_data.gameId,
                'ts': commentary_data.ts,
                'text': commentary_data.text,
                'severity': commentary_data.severity,
                'inning': commentary_data.inning,
                'half': commentary_data.half
                # eventType, playerName, teamName은 CommentaryEvent에 없으므로 제외
            }
            self.producer.send(self.commentary_topic, commentary_event)
                
                self.producer.flush()
                
            logging.info(f"📡 데이터 전송 완료:")
            logging.info(f"   - live-game-info: {live_game_data.status} {live_game_data.inning}회{live_game_data.half}")
            logging.info(f"   - commentary: {commentary_data.text}")
                    
            except Exception as e:
            logging.error(f"❌ Kafka 전송 실패: {e}")
    
    def simulate_single_game(self, game: dict):
        """단일 경기 시뮬레이션"""
        game_id = game["gameId"]
        home_team = game["homeTeam"]
        away_team = game["awayTeam"]
        max_innings = game["maxInnings"]
        
        logging.info(f"🎮 {away_team} vs {home_team} 경기 시작 (최대 {max_innings}회)")
        
        # 경기 시작 전
        start_commentary = self.create_commentary_data(
            game_id=game_id,
            text=f"{away_team} vs {home_team} 경기가 곧 시작됩니다.",
            inning=0,
            half="",
            event_type="GAME_START"
        )
        
        start_live_game = self.create_live_game_data(
            game_id=game_id,
            home_team=home_team,
            away_team=away_team,
            status="READY",
            inning=0,
            half="",
            home_score=0,
            away_score=0,
            bases=[False, False, False],
            outs=0
        )
        
        self.send_to_kafka(start_live_game, start_commentary)
        time.sleep(2)  # 경기 시작 전 잠시 대기
        
        # 경기 시작
        current_score = {"homeScore": 0, "awayScore": 0}
        current_inning = 1
        current_half = "T"  # 원정팀부터 시작
        
        while current_inning <= max_innings:
            # 이닝 시작 해설
            inning_text = f"{current_inning}회{current_half} {away_team if current_half == 'T' else home_team} 공격이 시작됩니다."
            if current_inning > 9:
                inning_text = f"연장 {current_inning}회{current_half} {away_team if current_half == 'T' else home_team} 공격이 시작됩니다."
            
            inning_commentary = self.create_commentary_data(
                game_id=game_id,
                text=inning_text,
                inning=current_inning,
                half=current_half,
                event_type="EXTRA_INNING" if current_inning > 9 else "INNING_CHANGE"
            )
            
            # 이닝 시뮬레이션
            inning_result = self._simulate_inning(
                game_id, home_team, away_team, 
                current_inning, current_half, current_score
            )
            
            # 이닝 결과 해설
            result_text = f"{current_inning}회{current_half} {inning_result['runs']}점을 냈습니다."
            if current_inning > 9:
                result_text = f"연장 {current_inning}회{current_half} {inning_result['runs']}점을 냈습니다."
            
            result_commentary = self.create_commentary_data(
                game_id=game_id,
                text=result_text,
                inning=current_inning,
                half=current_half,
                event_type="INNING_RESULT"
            )
            
            # 실시간 경기 데이터
            live_game_data = self.create_live_game_data(
                game_id=game_id,
                home_team=home_team,
                away_team=away_team,
                status="LIVE",
                inning=current_inning,
                half=current_half,
                home_score=current_score["homeScore"],
                away_score=current_score["awayScore"],
                bases=inning_result["bases"],
                outs=inning_result["outs"]
            )
            
            # 데이터 전송
            self.send_to_kafka(live_game_data, inning_commentary)
            time.sleep(1)
            self.send_to_kafka(live_game_data, result_commentary)
            
            logging.info(f"📡 {away_team} vs {home_team}: {current_inning}회{current_half} ({current_score['awayScore']}-{current_score['homeScore']})")
            
            # 이닝 변경
                        if current_half == "T":
                            current_half = "B"
                        else:
                current_half = "T"
                            current_inning += 1
            
            # 이닝 간 대기
            time.sleep(3)
            
            # 경기 종료
        final_commentary = self.create_commentary_data(
            game_id=game_id,
            text=f"경기가 종료되었습니다. {away_team} {current_score['awayScore']}-{current_score['homeScore']} {home_team}",
            inning=max_innings,
            half="B",
            event_type="GAME_END"
        )
        
        final_live_game = self.create_live_game_data(
            game_id=game_id,
            home_team=home_team,
            away_team=away_team,
            status="FINISHED",
            inning=max_innings,
            half="B",
            home_score=current_score["homeScore"],
            away_score=current_score["awayScore"],
            bases=[False, False, False],
            outs=3
        )
        
        self.send_to_kafka(final_live_game, final_commentary)
        
        winner = away_team if current_score['awayScore'] > current_score['homeScore'] else home_team
        logging.info(f"🏁 {away_team} vs {home_team} 경기 종료! {winner} 승리 ({current_score['awayScore']}-{current_score['homeScore']})")
    
    def simulate_all_games(self):
        """모든 경기 동시 시뮬레이션"""
        logging.info(f"🎮 {len(self.games)}개 경기 동시 시뮬레이션 시작")
        
        # 모든 경기를 동시에 시작
        threads = []
        for game in self.games:
            thread = threading.Thread(
                target=self.simulate_single_game,
                args=(game,),
                daemon=True
            )
            threads.append(thread)
            thread.start()
            logging.info(f"   - {game['awayTeam']} vs {game['homeTeam']} 스레드 시작")
        
        # 모든 경기가 완료될 때까지 대기
        for thread in threads:
            thread.join()
        
        logging.info("✅ 모든 경기 시뮬레이션 완료!")
    
    def run_once(self):
        """한 번의 시뮬레이션 실행"""
        if not self.setup_kafka():
            return False
            
        try:
            self.simulate_all_games()
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
        print("📊 시뮬레이션 내용:")
        print("   - 5개 경기가 동시에 시작")
        print("   - 4경기는 9회 정규 종료")
        print("   - 1경기는 연장전으로 11회까지 진행")
        print("📋 데이터 형식:")
        print("   - LiveGameData: 표준화된 실시간 경기 정보")
        print("   - CommentaryData: 표준화된 해설 메시지")
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
