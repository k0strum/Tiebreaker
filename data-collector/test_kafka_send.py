import json
import logging
from collectors.game_schedule_collector import collect_game_schedule, get_today_games_from_schedule

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)

def test_kafka_data_format():
    """
    Kafka로 전송될 데이터 형식을 테스트합니다.
    """
    print("=" * 60)
    print("🔍 Kafka 전송 데이터 형식 테스트")
    print("=" * 60)
    
    # 1. 전체 스케줄 데이터 수집
    print("\n📊 1. 전체 스케줄 데이터 수집")
    schedule_data = collect_game_schedule()
    
    if schedule_data.get('status') == 'success':
        print(f"✅ 전체 스케줄 수집 성공: {len(schedule_data.get('data', []))}개 경기")
        
        # Kafka로 전송될 전체 데이터 샘플 (첫 3개)
        sample_games = schedule_data.get('data', [])[:3]
        print(f"\n📋 전체 스케줄 데이터 샘플 (첫 3개):")
        for i, game in enumerate(sample_games, 1):
            print(f"  {i}. {game['awayTeamName']} vs {game['homeTeamName']} ({game['gameDate']}) - {game['statusCode']}")
        
        print(f"\n📄 Kafka 전송용 전체 데이터 구조:")
        print(json.dumps({
            "status": schedule_data.get('status'),
            "collected_at": schedule_data.get('collected_at'),
            "source": schedule_data.get('source'),
            "data_count": len(schedule_data.get('data', [])),
            "summary": schedule_data.get('summary')
        }, indent=2, ensure_ascii=False))
        
    else:
        print(f"❌ 전체 스케줄 수집 실패: {schedule_data.get('error')}")
        return
    
    # 2. 오늘 경기 데이터 필터링
    print("\n📊 2. 오늘 경기 데이터 필터링")
    today_data = get_today_games_from_schedule(schedule_data)
    
    if today_data.get('status') == 'success':
        print(f"✅ 오늘 경기 필터링 성공: {len(today_data.get('data', []))}개 경기")
        
        print(f"\n📋 오늘 경기 목록:")
        for i, game in enumerate(today_data.get('data', []), 1):
            print(f"  {i}. {game['awayTeamName']} vs {game['homeTeamName']} ({game['stadium']}) - {game['statusCode']}")
            print(f"     🏟️ {game['stadium']} | 📺 {game['broadChannel']} | 🏠 {game['homeStarterName']} | 🚌 {game['awayStarterName']}")
        
        print(f"\n📄 Kafka 전송용 오늘 경기 데이터 구조:")
        print(json.dumps({
            "status": today_data.get('status'),
            "collected_at": today_data.get('collected_at'),
            "source": today_data.get('source'),
            "date": today_data.get('date'),
            "data_count": len(today_data.get('data', [])),
            "summary": today_data.get('summary')
        }, indent=2, ensure_ascii=False))
        
    else:
        print(f"❌ 오늘 경기 필터링 실패: {today_data.get('error')}")
    
    print("\n" + "=" * 60)
    print("✅ Kafka 전송 데이터 형식 테스트 완료")
    print("=" * 60)

if __name__ == "__main__":
    test_kafka_data_format()
