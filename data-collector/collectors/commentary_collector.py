from __future__ import annotations

import json
import time
from typing import Optional

from api.commentary_api import CommentarySourceClient, normalize_event
from utils.kafka_producer import create_kafka_producer


class CommentaryCollector:
    def __init__(self, source_base_url: str, kafka_topic: str = "commentary"):
        self.client = CommentarySourceClient(source_base_url)
        self.kafka_topic = kafka_topic
        self.last_event_id_by_game: dict[str, Optional[str]] = {}

    def run_once(self, source_game_id: str, our_game_id: str):
        last_event_id = self.last_event_id_by_game.get(source_game_id)
        raws = self.client.fetch_events(source_game_id, last_event_id)
        if not raws:
            return 0
        producer = getattr(self, "_producer", None)
        if producer is None:
            # 기본 설정은 utils.config의 bootstrap_servers를 사용하되, 없으면 localhost:9092
            try:
                from utils.config import get_config
                bs = get_config()['kafka']['bootstrap_servers']
                producer = create_kafka_producer([bs])
            except Exception:
                producer = create_kafka_producer()
            self._producer = producer
        sent = 0
        for raw in raws:
            event = normalize_event(our_game_id, raw)
            self.last_event_id_by_game[source_game_id] = raw.get("sourceEventId")
            payload = {
                "gameId": event["gameId"],
                "ts": event["timestamp"],
                "text": event["text"],
                "severity": "INFO",
                "inning": event.get("inning"),
                "half": event.get("half"),
            }
            # utils.kafka_producer의 value_serializer가 json.dumps를 수행
            producer.send(self.kafka_topic, payload)
            sent += 1
        producer.flush()
        return sent

    def run_loop(self, pairs: list[tuple[str, str]], interval_sec: float = 2.0):
        while True:
            total = 0
            for source_game_id, our_game_id in pairs:
                try:
                    total += self.run_once(source_game_id, our_game_id)
                except Exception as e:
                    # 실제 환경에서는 로깅/알림
                    pass
            time.sleep(interval_sec)


