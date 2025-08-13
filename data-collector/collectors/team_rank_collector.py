import time
import requests
import logging

def collect_kbo_data():
    """
    네이버 스포츠에서 KBO 순위 데이터를 가져옵니다.
    
    Returns:
        dict: 수집된 데이터 또는 에러 정보
    """
    try:
        # 1. 목표 URL
        api_url = "https://api-gw.sports.naver.com/statistics/categories/kbo/seasons/2025/teams"
        
        # 2. requests 라이브러리로 웹페이지의 HTML 가져오기
        response = requests.get(api_url, headers={'User-Agent': 'Mozilla/5.0'})
        response.raise_for_status() # 오류가 있으면 바로 예외 발생
        
        data = response.json()
        if not data:
            raise ValueError("데이터를 찾을 수 없습니다.")
        
        teams = data.get('result', {}).get('seasonTeamStats', [])

        if not teams:
            raise ValueError("팀 데이터를 찾을 수 없습니다.")

        collected_data = []
        for team in teams:
            team_data = {
                'rank': team.get('ranking'),
                'teamName': team.get('teamName'),
                'plays': team.get('gameCount'),
                'wins': team.get('winGameCount'),
                'losses': team.get('loseGameCount'),
                'draws': team.get('drawnGameCount'),
                'winRate': team.get('wra'),
                'gameBehind': team.get('gameBehind'),
                'streak': team.get('continuousGameResult'),
            }

            collected_data.append(team_data)

        return {
            "status": "success",
            "collected_at": time.time(),
            "source": "naver_sports_api",
            "data": collected_data,
        }

    except Exception as e:
        # 오류 발생 시 로그를 남기고 빈 데이터를 반환합니다.
        logging.error(f"팀 랭킹 크롤링 중 오류 발생: {e}")
        return {"error": str(e)}
