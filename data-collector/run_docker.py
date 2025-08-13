#!/usr/bin/env python3
"""
선수 데이터 수집을 실행하는 스크립트 (도커 환경용)
"""
import os
import sys
import time
import json
from utils.kafka_producer import create_kafka_producer
from collectors.player_collector import scrape_all_players_and_details

def run_player_loading_task(producer):
    """
    모든 선수 마스터 데이터를 수집하여 Kafka로 전송하는 1회성 작업
    """
    # Kafka Producer가 정상적으로 생성되었는지 확인
    if not producer:
        print("Kafka Producer가 초기화되지 않았습니다. 작업을 중단합니다.")
        return

    print("선수 마스터 데이터 수집을 시작합니다...")

    # 가져온 producer를 사용하여 각 선수 정보를 Kafka로 보냅니다.
    for player in scrape_all_players_and_details():
        print(f"  -> {player.get('playerName', '알 수 없는 선수')} 정보 전송 중...")
        producer.send('kbo-player-data', value=player)

    producer.flush() # 모든 메시지가 전송되도록 보장
    print("모든 선수 마스터 데이터 전송 완료!")

if __name__ == '__main__':
    print("🐳 선수 데이터 수집을 시작합니다...")
    
    producer = None
    try:
        # --- 1. Kafka Producer 생성 ---
        print("🚚 Kafka Producer 연결을 시도합니다...")
        producer = create_kafka_producer()
        
        if producer is None:
            raise ConnectionError("Kafka Producer에 최종적으로 연결하지 못했습니다.")

        # --- 2. 생성된 Producer를 인자로 전달하여 작업 실행 ---
        print("=" * 60)
        print("👻 선수 데이터 수집을 시작합니다...")
        
        run_player_loading_task(producer)
        
        print("=" * 60)
        print("✅ 선수 데이터 수집이 완료되었습니다!")

    except Exception as e:
        print(f"❌ 작업 중 오류 발생: {e}")
        sys.exit(1)
        
    finally:
        # --- 3. 작업 완료 후 Producer 리소스 정리 ---
        if producer:
            print("👋 Kafka Producer 연결을 종료합니다.")
            producer.close()