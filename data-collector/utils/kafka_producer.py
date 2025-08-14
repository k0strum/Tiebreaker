import time
import json
import logging
from kafka import KafkaProducer

def create_kafka_producer(bootstrap_servers=['localhost:9092'], max_retries=10):
    """
    Kafka Producer를 생성하고 연결을 시도합니다.
    
    Args:
        bootstrap_servers: Kafka 서버 주소 리스트
        max_retries: 최대 재시도 횟수
    
    Returns:
        KafkaProducer 인스턴스 또는 None (실패 시)
    """
    retries = max_retries
    while retries > 0:
        try:
            producer = KafkaProducer(
                bootstrap_servers=bootstrap_servers,
                value_serializer=lambda v: json.dumps(v).encode('utf-8'),
                api_version=(0, 10, 2)
            )
            logging.info("Kafka Producer에 성공적으로 연결되었습니다.")
            return producer
        except Exception as e:
            retries -= 1
            logging.error(f"Kafka 연결 실패. 5초 후 {retries}번 더 재시도합니다... 오류: {e}")
            if retries > 0:
                time.sleep(5)
    
    logging.critical("Kafka Producer에 최종적으로 연결하지 못했습니다.")
    return None
