import time
import requests
from typing import List, Dict, Any


class CommentarySourceClient:
    def __init__(self, base_url: str, timeout: float = 5.0):
        self.base_url = base_url.rstrip('/')
        self.timeout = timeout

    def fetch_events(self, source_game_id: str, last_event_id: str | None) -> List[Dict[str, Any]]:
        # TODO: 실제 소스 API에 맞게 구현
        # 예시: GET {base_url}/games/{source_game_id}/commentary?afterId=last_event_id
        # 여기서는 더미 데이터를 반환
        now_ms = int(time.time() * 1000)
        return [{
            "sourceEventId": str(now_ms),
            "text": "더미 이벤트",
            "inning": 1,
            "half": "T",
            "ts": now_ms
        }]


def normalize_event(game_id: str, raw: Dict[str, Any]) -> Dict[str, Any]:
    return {
        "gameId": game_id,
        "timestamp": int(raw.get("ts")),
        "inning": int(raw.get("inning", 0)),
        "half": raw.get("half", "T"),
        "text": str(raw.get("text", "")),
        "eventType": raw.get("eventType", "INFO"),
        "raw": {
            "sourceEventId": raw.get("sourceEventId")
        }
    }


