import os

def get_config():
    """
    로컬 환경 설정을 반환합니다.
    """
    return {
        'kafka': {
            'bootstrap_servers': 'localhost:9092',
            'topic_player_data': 'kbo-player-data',
            'topic_team_rank_data': 'kbo-team-rank-data'
        },
        'webdriver': {
            'headless': False,  # 로컬에서는 브라우저 창을 보는 것이 디버깅에 도움
            'timeout': 20
        },
        'scraping': {
            'delay_between_requests': 2,  # 요청 간 지연 시간 (초)
            'max_retries': 3
        }
    }

# 환경변수로 설정을 오버라이드할 수 있도록 함
def get_kafka_config():
    """Kafka 설정을 반환합니다."""
    config = get_config()
    return {
        'bootstrap_servers': os.environ.get('KAFKA_BOOTSTRAP_SERVERS', config['kafka']['bootstrap_servers']),
        'topic_player_data': os.environ.get('KAFKA_TOPIC_PLAYER_DATA', config['kafka']['topic_player_data']),
        'topic_team_rank_data': os.environ.get('KAFKA_TOPIC_TEAM_RANK_DATA', config['kafka']['topic_team_rank_data'])
    }
