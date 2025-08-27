import json
import logging
from kafka import KafkaConsumer

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')


def read_one(topic, group_id):
    consumer = KafkaConsumer(
        topic,
        bootstrap_servers=['localhost:9092'],
        group_id=group_id,
        auto_offset_reset='earliest',
        enable_auto_commit=False,
        value_deserializer=lambda m: json.loads(m.decode('utf-8')),
        consumer_timeout_ms=3000,
        api_version=(0, 10, 2),
    )
    try:
        for msg in consumer:
            print(f"\n=== Topic: {topic} | Partition: {msg.partition} | Offset: {msg.offset} ===")
            print(json.dumps(msg.value, indent=2, ensure_ascii=False))
            break
        else:
            print(f"(no message) topic={topic}")
    finally:
        consumer.close()


if __name__ == '__main__':
    print("읽기: kbo-game-schedule")
    read_one('kbo-game-schedule', 'verify-group-schedule')
    print("\n읽기: kbo-today-games")
    read_one('kbo-today-games', 'verify-group-today')
