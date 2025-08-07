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
    이 함수 안에 실제 웹사이트에서 데이터를 가져오는 코드를 작성합니다.
    """
    try:
        # **여기에 크롤링할 웹사이트 주소를 넣습니다.**
        url = "https://sports.news.naver.com/kbaseball/record/index?category=kbo"
        
        # requests 라이브러리로 웹페이지의 HTML을 가져옵니다.
        response = requests.get(url)
        # 인코딩 문제가 발생하면 아래 코드를 사용합니다.
        # response.raise_for_status() # 오류가 있으면 바로 예외 발생

        response.encoding = 'utf-8'
        
        # BeautifulSoup으로 HTML을 파싱(분석)하기 쉽게 만듭니다.
        soup = BeautifulSoup(response.text, 'html.parser')

        # **이제부터 soup 객체를 이용해 원하는 정보를 추출합니다.**
        # 예시: KBO 팀 순위 테이블 추출 (선택자는 실제 사이트 구조에 맞게 변경해야 함)
        team_list = []
        # for row in soup.select('#regularTeamRecordList_table > tr'):
        #     rank = row.select_one('th > strong').text
        #     team_name = row.select_one('td.tm > div > span').text
        #     team_list.append({'rank': rank, 'name': team_name})

        # 지금은 실제 크롤링 대신 테스트용 예시 데이터를 반환합니다.
        example_data = {
            'collected_at': time.time(),
            'source': 'Naver Sports',
            'data': [
                {'rank': 1, 'name': '한화'},
                {'rank': 2, 'name': 'LG'},
                {'rank': 3, 'name': '롯데'}
            ]
        }
        return example_data

    except Exception as e:
        # 오류 발생 시 로그를 남기고 빈 데이터를 반환합니다.
        print(f"크롤링 중 오류 발생: {e}")
        return {"error": str(e)}

# trigger_collection 함수를 수정하여 Kafka로 데이터를 전송하도록 변경
@app.route('/api/collect')
def trigger_collection():
    collected_data = collect_kbo_data()
    
    # 'kbo-rank-data' 라는 토픽으로 수집된 데이터를 전송합니다.
    producer.send('kbo-rank-data', value=collected_data)
    producer.flush() # 메시지가 완전히 전송되도록 보장

    print(f"Sent to Kafka: {collected_data}")
    return jsonify({"status": "success", "message": "Data sent to Kafka successfully."})

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