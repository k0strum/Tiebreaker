from flask import Blueprint, jsonify
from collectors.player_collector import scrape_all_players_and_details
from utils.kafka_producer import create_kafka_producer

# Blueprint 생성
player_bp = Blueprint('player', __name__)

@player_bp.route('/api/collect/players', methods=['POST'])
def trigger_player_collection():
    """
    선수 정보 데이터 수집을 수동으로 트리거합니다.
    """
    try:
        # Kafka Producer 생성
        producer = create_kafka_producer()
        if not producer:
            return jsonify({
                "status": "error",
                "message": "Kafka Producer 연결 실패"
            }), 500
        
        # 선수 데이터 수집 및 전송
        player_count = 0
        for player in scrape_all_players_and_details():
            producer.send('kbo-player-data', value=player)
            player_count += 1
        
        producer.flush()
        producer.close()
        
        return jsonify({
            "status": "success",
            "message": f"선수 정보가 성공적으로 수집되고 Kafka로 전송되었습니다. (총 {player_count}명)",
            "player_count": player_count
        })
        
    except Exception as e:
        return jsonify({
            "status": "error",
            "message": f"선수 정보 수집 중 오류 발생: {str(e)}"
        }), 500
