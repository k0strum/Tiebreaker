from flask import Flask, jsonify
import requests
from bs4 import BeautifulSoup
import time
import json
from kafka import KafkaProducer

# Flask 애플리케이션 인스턴스 생성  
app = Flask(__name__)

# Kafka Producer 인스턴스 생성
# value_serializer는 메시지 값을 JSON 형태로 직렬화하고 UTF-8로 인코딩합니다.
producer = KafkaProducer(
    bootstrap_servers=['kafka:9092'], # docker-compose에 정의된 kafka 서비스 이름 사용
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

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

# 이 파일이 직접 실행될 때만 Flask 서버 실행
if __name__ == '__main__':  
    app.run(host='0.0.0.0', port=5000, debug=True)