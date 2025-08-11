from flask import Flask, jsonify
import requests
# from bs4 import BeautifulSoup
import time
import json
from kafka import KafkaProducer
from apscheduler.schedulers.background import BackgroundScheduler
import atexit
import logging

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

# Flask 애플리케이션 인스턴스 생성  
app = Flask(__name__)

# --- [수정] Kafka Producer 생성 부분에 재시도 로직 추가 ---
producer = None
retries = 10 # 10번 재시도
while retries > 0:
    try:
        producer = KafkaProducer(
            bootstrap_servers=['kafka:9092'],
            value_serializer=lambda v: json.dumps(v).encode('utf-8'),
            api_version=(0, 10, 2) # 특정 버전을 명시하여 호환성 문제 방지
        )
        logging.info("Kafka Producer에 성공적으로 연결되었습니다.")
        break  # 성공 시 루프 탈출
    except Exception as e:
        retries -= 1
        logging.error(f"Kafka 연결 실패. 5초 후 {retries}번 더 재시도합니다... 오류: {e}")
        time.sleep(5) # 5초 대기 후 재시도

if producer is None:
    logging.critical("Kafka Producer에 최종적으로 연결하지 못했습니다. 스케줄러를 시작할 수 없습니다.")
    # 이 경우, 앱이 정상 동작할 수 없음을 의미합니다.

# 1. 실제 데이터 수집을 담당할 함수
def collect_kbo_data():
    """
    네이버 스포츠에서 kbo 순위 데이터를 가져옵니다.
    """
    try:
        # 1. 목표 URL
        api_url = "https://api-gw.sports.naver.com/statistics/categories/kbo/seasons/2025/teams"
        
        # 2. requests 라이브러리로 웹페이지의 HTML 가져오기
        response = requests.get(api_url, headers={'User-Agent': 'Mozilla/5.0'})
        response.raise_for_status() # 오류가 있으면 바로 예외 발생
        
        data = response.json()
        if not data:
            raise ValueError("데이터를 찾을 수 없습니다.")
        print(data)
        
        teams = data.get('result', {}).get('seasonTeamStats', [])

        if not teams:
            raise ValueError("팀 데이터를 찾을 수 없습니다.")

        collected_data = []
        for team in teams:
            team_data = {
                'rank': team.get('ranking'),
                'teamName': team.get('teamName'),
                'plays': team.get('gameCount'),
                'wins': team.get('winGameCount'),
                'losses': team.get('loseGameCount'),
                'draws': team.get('drawnGameCount'),
                'winRate': team.get('wra'),
                'gameBehind': team.get('gameBehind'),
                'streak': team.get('continuousGameResult'),
            }

            collected_data.append(team_data)

        return {
            "status": "success",
            "collected_at": time.time(),
            "source": "naver_sports_api",
            "data": collected_data,
        }

    except Exception as e:
        # 오류 발생 시 로그를 남기고 빈 데이터를 반환합니다.
        print(f"크롤링 중 오류 발생: {e}")
        return {"error": str(e)}

def schedule_collection():
    print("Data schedule collection start")
    collected_data = collect_kbo_data()
    
    if collected_data.get('status') == 'success':
        producer.send('kbo-rank-data', value=collected_data)
        producer.flush()
        print(f"Schedule job sent to Kafka")
    else:
        print(f"Failed to collect data: {collected_data.get('error')}")

# kafka 메시지 전송
@app.route('/api/collect')
def trigger_collection():
    collected_data = collect_kbo_data()
    
    # 'kbo-rank-data' 라는 토픽으로 수집된 데이터를 전송합니다.
    producer.send('kbo-rank-data', value=collected_data)
    producer.flush() # 메시지가 완전히 전송되도록 보장

    print(f"Sent to Kafka: {collected_data}")
    return jsonify({"status": "success", "message": "Data sent to Kafka successfully."})

# 데이터 수집 테스트용
# @app.route('/api/collect')
# def trigger_collection():
#     result = collect_kbo_data()
#     return jsonify(result)

# 루트 경로에 대한 라우트 핸들러 정의
@app.route('/')
def home():
    return "Data Collector Server is running!"

# 상태 확인 엔드포인트
@app.route('/health')
def health():
    return jsonify({"status": "ok"})

# producer가 성공적으로 연결되었을 때만 스케줄러를 실행
if producer:
    scheduler = BackgroundScheduler()
    scheduler.add_job(schedule_collection, 'interval', minutes=60)
    scheduler.start()
    logging.info("APScheduler가 시작되었습니다. 2분 간격으로 작업을 실행합니다.")
    
    # 앱 종료 시 스케줄러도 함께 종료되도록 설정
    atexit.register(lambda: scheduler.shutdown())
else:
    logging.critical("Kafka에 연결되지 않아 스케줄러를 시작할 수 없습니다.")


# 이 파일이 "직접" python app.py로 실행될 때만 Flask 개발 서버를 구동합니다.
# Docker에서 flask run으로 실행될 때는 이 부분이 실행되지 않고,
# 대신 Dockerfile의 CMD 명령어가 Flask 서버를 실행합니다.
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True, use_reloader=False)