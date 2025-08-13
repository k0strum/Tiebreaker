#!/usr/bin/env python3
"""
도커 컨테이너 환경에서 선수 데이터 수집을 실행하는 스크립트
"""
import os
import sys
import time
import json
from kafka import KafkaProducer

# 환경 변수 설정
os.environ['ENVIRONMENT'] = 'docker'

# 현재 디렉토리를 Python 경로에 추가
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from load_players import run_player_loading_task

if __name__ == '__main__':
    print("🐳 도커 컨테이너 환경에서 선수 데이터 수집을 시작합니다...")
    
    producer = None
    try:
        # --- 1. Kafka Producer 생성 (app.py와 동일한 재시도 로직) ---
        print("🚚 Kafka Producer 연결을 시도합니다...")
        retries = 10
        while retries > 0:
            try:
                producer = KafkaProducer(
                    bootstrap_servers=['kafka:9092'],
                    value_serializer=lambda v: json.dumps(v).encode('utf-8'),
                    api_version=(0, 10, 2)
                )
                print("👍 Kafka Producer에 성공적으로 연결되었습니다.")
                break 
            except Exception as e:
                retries -= 1
                print(f"   - Kafka 연결 실패. 5초 후 재시도합니다... (남은 횟수: {retries})")
                time.sleep(5)
        
        if producer is None:
            raise ConnectionError("Kafka Producer에 최종적으로 연결하지 못했습니다.")

        # --- 2. 생성된 Producer를 인자로 전달하여 작업 실행 ---
        print("=" * 60)
        print("👻 선수 데이터 수집을 시작합니다...")
        
        run_player_loading_task(producer) # producer를 인자로 전달!
        
        print("=" * 60)
        print("✅ 도커 환경에서 선수 데이터 수집이 완료되었습니다!")

    except Exception as e:
        print(f"❌ 작업 중 오류 발생: {e}")
        sys.exit(1)
        
    finally:
        # --- 3. 작업 완료 후 Producer 리소스 정리 ---
        if producer:
            print("👋 Kafka Producer 연결을 종료합니다.")
            producer.close()