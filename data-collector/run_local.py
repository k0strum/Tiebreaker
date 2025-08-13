#!/usr/bin/env python3
"""
로컬 환경에서 선수 데이터 수집을 실행하는 스크립트
"""
import os
import sys

# 환경 변수 설정
os.environ['ENVIRONMENT'] = 'local'

# 현재 디렉토리를 Python 경로에 추가
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from load_players import run_player_loading_task

if __name__ == '__main__':
    print("🚀 로컬 환경에서 선수 데이터 수집을 시작합니다...")
    print("📁 Chrome 경로: C:\\Kostrum\\Tiebreaker\\data-collector\\chrome-win64\\chrome.exe")
    print("📁 ChromeDriver 경로: C:\\Kostrum\\Tiebreaker\\data-collector\\chromedriver-win64\\chromedriver.exe")
    print("=" * 60)
    
    try:
        run_player_loading_task()
        print("✅ 로컬 환경에서 선수 데이터 수집이 완료되었습니다!")
    except Exception as e:
        print(f"❌ 오류 발생: {e}")
        sys.exit(1)
