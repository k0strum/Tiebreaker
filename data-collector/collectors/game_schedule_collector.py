import time
import requests
import logging
import json
import calendar
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

def _fetch_games(from_date: str, to_date: str) -> list:
    """
    주어진 날짜 구간에 대해 Naver games API를 호출해 원시 game 리스트를 반환합니다.

    Args:
        from_date (str): 'YYYY-MM-DD'
        to_date (str): 'YYYY-MM-DD'

    Returns:
        list: games 배열 (빈 배열 가능)
    """
    api_url = (
        'https://api-gw.sports.naver.com/schedule/games'
        f'?fields=basic%2Cschedule%2Cbaseball&upperCategoryId=kbaseball&categoryId=kbo'
        f'&fromDate={from_date}&toDate={to_date}&roundCodes=&size=500'
    )

    logging.info(f"🔍 구간 수집: {from_date} ~ {to_date}")
    response = requests.get(api_url, headers={'User-Agent': 'Mozilla/5.0'})
    response.raise_for_status()
    data = response.json()
    if not data or not data.get('success'):
        return []
    result = data.get('result', {})
    games = result.get('games', [])
    logging.info(f"📦 구간 수신 경기 수: {len(games)} (gameTotalCount={result.get('gameTotalCount')})")
    return games


def collect_game_schedule():
    """
    네이버 스포츠 games API를 사용하여 KBO 연간 전체 일정을 수집합니다.
    KBO 시즌(3월~10월)의 모든 경기 일정을 수집하여 반환합니다.
    
    Returns:
        dict: 수집된 데이터 또는 에러 정보
    """
    try:
        # 조회 기간: 연간 전체 (3월 ~ 10월 KBO 시즌)
        now = datetime.now()
        year = now.year
        
        # KBO 시즌은 보통 3월에 시작해서 10월에 끝남
        season_start = datetime(year, 3, 1)  # 3월 1일
        season_end = datetime(year, 10, 31)  # 10월 31일
        
        # 만약 현재 시즌이 끝났다면 다음 시즌을 조회
        if now > season_end:
            year += 1
            season_start = datetime(year, 3, 1)
            season_end = datetime(year, 10, 31)

        logging.info(f"🔍 KBO 연간 경기 일정 수집 시작: {season_start.strftime('%Y-%m-%d')} ~ {season_end.strftime('%Y-%m-%d')}")

        # 월별 구간으로 나누어 수집 (size=500 제한 회피)
        pointer = season_start
        all_games = []
        while pointer <= season_end:
            year = pointer.year
            month = pointer.month
            start_day = 1 if pointer.day == 1 else pointer.day
            start_date = datetime(year, month, start_day)
            last_day = calendar.monthrange(year, month)[1]
            end_date = datetime(year, month, last_day)
            # 시즌 끝 이후로 넘어가지 않도록 캡
            if end_date > season_end:
                end_date = season_end

            from_date = start_date.strftime('%Y-%m-%d')
            to_date = end_date.strftime('%Y-%m-%d')
            try:
                monthly_games = _fetch_games(from_date, to_date)
                all_games.extend(monthly_games)
            except Exception as e:
                logging.error(f"❌ {from_date}~{to_date} 구간 수집 실패: {e}")

            # 다음 달 1일로 이동
            if month == 12:
                pointer = datetime(year + 1, 1, 1)
            else:
                pointer = datetime(year, month + 1, 1)

        # 중복 제거 (gameId 기준)
        unique_by_id = {}
        for g in all_games:
            gid = g.get('gameId')
            if gid:
                unique_by_id[gid] = g

        games = list(unique_by_id.values())
        logging.info(f"📦 통합 수신 경기 수(중복 제거): {len(games)}")

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
                    'start': season_start.strftime('%Y-%m-%d'),
                    'end': season_end.strftime('%Y-%m-%d'),
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
    연간 경기 스케줄 수집 테스트 함수
    """
    print("=" * 60)
    print("🏟️ KBO 연간 경기 스케줄 수집 테스트 시작")
    print("=" * 60)
    
    result = collect_game_schedule()
    
    if result.get('status') == 'success':
        print(f"✅ 수집 성공!")
        print(f"📊 수집된 경기 수: {len(result.get('data', []))}")
        print(f"📅 시즌 기간: {result.get('summary', {}).get('date_range', {}).get('start')} ~ {result.get('summary', {}).get('date_range', {}).get('end')}")
        
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
