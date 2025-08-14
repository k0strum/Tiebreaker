from flask import Blueprint, jsonify
from collectors.team_rank_collector import collect_kbo_data
from utils.kafka_producer import create_kafka_producer

# Blueprint 생성
team_rank_bp = Blueprint('team_rank', __name__)

@team_rank_bp.route('/api/collect/team-rank', methods=['POST', 'GET'])
def trigger_team_rank_collection():
    """
    팀 랭킹 데이터 수집을 수동으로 트리거합니다.
    """
    try:
        # 데이터 수집
        collected_data = collect_kbo_data()
        
        # Kafka로 전송
        producer = create_kafka_producer()
        if producer:
            producer.send('kbo-rank-data', value=collected_data)
            producer.flush()
            producer.close()
            
            return jsonify({
                "status": "success", 
                "message": "팀 랭킹 데이터가 성공적으로 수집되고 Kafka로 전송되었습니다.",
                "data": collected_data
            })
        else:
            return jsonify({
                "status": "error",
                "message": "Kafka Producer 연결 실패"
            }), 500
            
    except Exception as e:
        return jsonify({
            "status": "error",
            "message": f"데이터 수집 중 오류 발생: {str(e)}"
        }), 500
