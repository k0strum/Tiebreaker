import time
import requests
import logging
import json
from datetime import datetime, timedelta

# 팀 코드를 팀명으로 변환하는 매핑
TEAM_CODE_MAPPING = {
    'HH': '한화',
    'HT': '기아',
    'KT': 'KT',
    'LG': 'LG',
    'LT': '롯데',
    'NC': 'NC',
    'OB': '두산',
    'SK': 'SSG',
    'SS': '삼성',
    'WO': '키움'
}

def is_valid_kbo_game(game_info):
    """
    실제 KBO 정규 경기인지 확인하는 함수
    
    Args:
        game_info: 경기 정보 딕셔너리
        
    Returns:
        bool: 실제 KBO 정규 경기면 True, 아니면 False
    """
    home_team_code = game_info.get('homeTeamCode', '')
    away_team_code = game_info.get('awayTeamCode', '')
    
    # 1. 홈팀과 원정팀 코드가 모두 유효한 팀 코드인지 확인
    valid_team_codes = set(TEAM_CODE_MAPPING.keys())
    if home_team_code not in valid_team_codes or away_team_code not in valid_team_codes:
        return False
    
    # 2. 홈팀과 원정팀이 같은 팀이 아닌지 확인
    if home_team_code == away_team_code:
        return False
    
    # 3. 빈 문자열이 아닌지 확인
    if not home_team_code or not away_team_code:
        return False
    
    return True

def collect_game_schedule():
    """
    네이버 스포츠 games API를 사용하여 KBO 월별 전체 일정을 수집합니다.
    요청한 최소 스키마로 매핑하여 반환합니다.
    
    Returns:
        dict: 수집된 데이터 또는 에러 정보
    """
    try:
        # 조회 기간: 현재 달의 1일 ~ 말일
        now = datetime.now()
        month_start = datetime(now.year, now.month, 1)
        # 다음 달 1일에서 하루 빼기 = 말일
        if now.month == 12:
            next_month_start = datetime(now.year + 1, 1, 1)
        else:
            next_month_start = datetime(now.year, now.month + 1, 1)
        month_end = next_month_start - timedelta(days=1)

        from_date = month_start.strftime('%Y-%m-%d')
        to_date = month_end.strftime('%Y-%m-%d')

        api_url = (
            'https://api-gw.sports.naver.com/schedule/games'
            f'?fields=basic%2Cschedule%2Cbaseball&upperCategoryId=kbaseball&categoryId=kbo'
            f'&fromDate={from_date}&toDate={to_date}&roundCodes=&size=500'
        )

        logging.info(f"🔍 KBO games 수집 시작: {from_date} ~ {to_date}")

        response = requests.get(api_url, headers={'User-Agent': 'Mozilla/5.0'})
        response.raise_for_status()
        logging.info(f"✅ API 응답 성공: {response.status_code}")

        data = response.json()
        if not data or not data.get('success'):
            raise ValueError('데이터를 찾을 수 없습니다.')

        result = data.get('result', {})
        games = result.get('games', [])
        logging.info(f"📦 수신 경기 수: {len(games)} (gameTotalCount={result.get('gameTotalCount')})")

        mapped = []
        for g in games:
            # 요청한 필드로만 매핑
            mapped.append({
                'gameId': g.get('gameId', ''),
                'gameDate': g.get('gameDate', ''),
                'gameDateTime': g.get('gameDateTime', ''),
                'stadium': g.get('stadium', ''),
                'homeTeamCode': g.get('homeTeamCode', ''),
                'homeTeamName': g.get('homeTeamName', ''),
                'homeTeamScore': g.get('homeTeamScore', 0),
                'awayTeamCode': g.get('awayTeamCode', ''),
                'awayTeamName': g.get('awayTeamName', ''),
                'awayTeamScore': g.get('awayTeamScore', 0),
                'statusCode': g.get('statusCode', ''),
                'statusInfo': g.get('statusInfo', ''),  # 경기 진행 상황 추가
                'winner': g.get('winner', ''),
                'suspended': g.get('suspended', False),
                'broadChannel': g.get('broadChannel', ''),
                'homeStarterName': g.get('homeStarterName', ''),
                'awayStarterName': g.get('awayStarterName', ''),
                'roundCode': g.get('roundCode', ''),
            })

        # 정렬: 날짜/시간 기준
        mapped.sort(key=lambda x: (x.get('gameDate', ''), x.get('gameDateTime', '')))

        return {
            'status': 'success',
            'collected_at': time.time(),
            'source': 'naver_sports_api_games',
            'data': mapped,
            'summary': {
                'total_games': len(mapped),
                'date_range': {
                    'start': from_date,
                    'end': to_date,
                }
            }
        }

    except Exception as e:
        logging.error(f"❌ games API 수집 중 오류: {e}")
        return {'error': str(e)}

def get_today_games_from_schedule(schedule_data):
    """
    전체 스케줄 데이터에서 오늘 경기만 필터링하는 함수
    
    Args:
        schedule_data: collect_game_schedule()에서 반환된 데이터
        
    Returns:
        dict: 오늘 경기만 필터링된 데이터
    """
    if schedule_data.get('status') != 'success':
        return schedule_data
    
    today = datetime.now()
    today_str = today.strftime('%Y-%m-%d')
    
    today_games = [
        game for game in schedule_data.get('data', [])
        if game.get('gameDate') == today_str
    ]
    
    return {
        "status": "success",
        "collected_at": time.time(),
        "source": "naver_sports_api",
        "data": today_games,
        "date": today_str,
        "summary": {
            "total_games": len(today_games),
            "date": today_str
        }
    }

def test_collect_game_schedule():
    """
    경기 스케줄 수집 테스트 함수
    """
    print("=" * 60)
    print("🏟️ KBO 경기 스케줄 수집 테스트 시작")
    print("=" * 60)
    
    result = collect_game_schedule()
    
    if result.get('status') == 'success':
        print(f"✅ 수집 성공!")
        print(f"📊 수집된 경기 수: {len(result.get('data', []))}")
        print(f"📅 기간: {result.get('summary', {}).get('date_range', {}).get('start')} ~ {result.get('summary', {}).get('date_range', {}).get('end')}")
        print(f"🚫 필터링된 경기 수: {result.get('summary', {}).get('filtered_games', 0)}")
        
        print("\n📋 수집된 경기 목록 (최근 10개):")
        recent_games = result.get('data', [])[-10:]  # 최근 10개만 표시
        for i, game in enumerate(recent_games, 1):
            when = game.get('gameDateTime') or game.get('gameDate')
            print(f"  {i}. {game['awayTeamName']} vs {game['homeTeamName']} ({when}) - {game['statusCode']}")
        
        if len(result.get('data', [])) > 10:
            print(f"  ... 외 {len(result.get('data', [])) - 10}개 경기")
        
        print(f"\n📄 전체 데이터 (JSON):")
        print(json.dumps(result, indent=2, ensure_ascii=False))
    else:
        print(f"❌ 수집 실패: {result.get('error')}")
    
    print("=" * 60)
    return result

def test_today_games_filtering():
    """
    오늘 경기 필터링 테스트 함수
    """
    print("=" * 60)
    print("⚾ 오늘 경기 필터링 테스트 시작")
    print("=" * 60)
    
    # 먼저 전체 스케줄을 수집
    schedule_result = collect_game_schedule()
    
    if schedule_result.get('status') != 'success':
        print(f"❌ 스케줄 수집 실패: {schedule_result.get('error')}")
        return schedule_result
    
    # 오늘 경기만 필터링
    today_result = get_today_games_from_schedule(schedule_result)
    
    if today_result.get('status') == 'success':
        print(f"✅ 필터링 성공!")
        print(f"📊 오늘 경기 수: {len(today_result.get('data', []))}")
        print(f"📅 날짜: {today_result.get('date')}")
        
        print("\n📋 오늘 경기 목록:")
        for i, game in enumerate(today_result.get('data', []), 1):
            print(f"  {i}. {game['awayTeamName']} vs {game['homeTeamName']} - {game['statusCode']}")
        
        print(f"\n📄 오늘 경기 데이터 (JSON):")
        print(json.dumps(today_result, indent=2, ensure_ascii=False))
    else:
        print(f"❌ 필터링 실패: {today_result.get('error')}")
    
    print("=" * 60)
    return today_result

if __name__ == "__main__":
    # 로깅 설정
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(levelname)s - %(message)s'
    )
    
    # 테스트 실행
    test_today_games_filtering()
    print("\n")
    test_collect_game_schedule()
