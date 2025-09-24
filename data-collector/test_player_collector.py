#!/usr/bin/env python3
"""
선수 수집기 테스트 스크립트
컨테이너에서 직접 실행하여 선수 수집 과정을 테스트할 수 있습니다.

사용법:
1. 컨테이너 내부에서 실행:
   docker compose exec data-collector python test_player_collector.py

2. 특정 팀만 테스트:
   docker compose exec data-collector python test_player_collector.py --team SK

3. 선수 수 제한:
   docker compose exec data-collector python test_player_collector.py --limit 5

4. 헤드리스 모드 비활성화:
   docker compose exec data-collector python test_player_collector.py --no-headless
"""

import sys
import os
import argparse
import logging
from collections import Counter

# 현재 디렉토리를 Python 경로에 추가
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from collectors.player_collector import scrape_all_players_and_details, TEAM_CODE_TO_NAME
from utils.config import get_config

def setup_logging():
    """로깅 설정"""
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(levelname)s - %(message)s',
        handlers=[
            logging.StreamHandler(sys.stdout)
        ]
    )
    return logging.getLogger(__name__)

def test_single_team(team_code, limit=None, no_headless=False):
    """단일 팀 테스트"""
    logger = setup_logging()
    logger.info(f"=== {team_code} 팀 테스트 시작 ===")
    
    # 설정 수정
    config = get_config()
    if no_headless:
        config['webdriver']['headless'] = False
        logger.info("헤드리스 모드 비활성화")
    
    # 임시로 팀 코드를 단일 팀으로 제한
    original_scrape_func = scrape_all_players_and_details
    
    def limited_scrape():
        print("[DEBUG] ========== 테스트용 제한된 선수 수집 시작 ==========")
        print(f"[DEBUG] 테스트 대상 팀: {team_code}")
        
        # 원본 함수의 로직을 복사하되 팀 코드를 제한
        from selenium import webdriver
        from selenium.webdriver.support.ui import WebDriverWait
        from selenium.webdriver.support import expected_conditions as EC
        from selenium.webdriver.common.by import By
        from selenium.webdriver.support.ui import Select
        from selenium.common.exceptions import TimeoutException
        from bs4 import BeautifulSoup
        import time
        import random
        
        team_name = TEAM_CODE_TO_NAME.get(team_code, f"팀코드_{team_code}")
        search_url = "https://www.koreabaseball.com/Player/Search.aspx"
        
        options = webdriver.ChromeOptions()
        if config['webdriver']['headless']:
            options.add_argument('--headless')
        options.add_argument('--no-sandbox')
        options.add_argument('--disable-dev-shm-usage')
        options.add_argument('--window-size=1920,1080')
        options.add_argument('--disable-gpu')
        options.add_argument('--disable-extensions')
        options.add_argument('--disable-plugins')
        options.add_argument('--disable-images')
        # options.add_argument('--disable-javascript')  # 크롤링에 필요하므로 주석 처리
        options.add_argument('--disable-web-security')
        options.add_argument('--allow-running-insecure-content')
        options.add_argument('--disable-features=VizDisplayCompositor')
        options.add_argument('user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36')
        
        driver = None
        try:
            driver = webdriver.Chrome(options=options)
            wait = WebDriverWait(driver, 15)
            
            # 팀 페이지로 이동
            driver.get(search_url)
            Select(wait.until(EC.presence_of_element_located((By.ID, 'cphContents_cphContents_cphContents_ddlTeam')))).select_by_value(team_code)
            
            # 선수 링크 수집
            from collectors.player_collector import get_player_links
            all_links = get_player_links(driver, wait)
            
            logger.info(f"{team_code} 팀에서 발견된 선수 수: {len(all_links)}명")
            
            if limit:
                all_links = all_links[:limit]
                logger.info(f"제한 적용: {limit}명만 테스트")
            
            collected_count = 0
            for i, link in enumerate(all_links):
                try:
                    player_name = link.text.strip()
                    logger.info(f"테스트 중: {i+1}/{len(all_links)} - {player_name}")
                    
                    # 링크 클릭
                    wait.until(EC.element_to_be_clickable(link))
                    driver.execute_script("arguments[0].click();", link)
                    time.sleep(random.uniform(1, 2))
                    
                    # 페이지 로딩 확인
                    try:
                        wait.until(EC.presence_of_element_located((By.CLASS_NAME, "player_basic")))
                        logger.info(f"  ✓ {player_name} 페이지 로딩 성공")
                        collected_count += 1
                    except TimeoutException:
                        logger.warning(f"  ✗ {player_name} 페이지 로딩 실패")
                    
                    # 검색 페이지로 돌아가기
                    driver.get(search_url)
                    Select(wait.until(EC.presence_of_element_located((By.ID, 'cphContents_cphContents_cphContents_ddlTeam')))).select_by_value(team_code)
                    
                except Exception as e:
                    logger.error(f"  ✗ {player_name} 처리 중 오류: {e}")
                    continue
            
            logger.info(f"=== {team_code} 팀 테스트 완료 ===")
            logger.info(f"성공: {collected_count}/{len(all_links)}명")
            
        except Exception as e:
            logger.error(f"테스트 중 오류 발생: {e}")
            import traceback
            logger.error(traceback.format_exc())
        finally:
            if driver:
                driver.quit()
    
    # 제한된 함수 실행
    limited_scrape()

def test_kafka_connection():
    """Kafka 연결 테스트"""
    logger = setup_logging()
    logger.info("=== Kafka 연결 테스트 시작 ===")
    
    try:
        from utils.kafka_producer import create_kafka_producer
        config = get_config()
        
        producer = create_kafka_producer([config['kafka']['bootstrap_servers']])
        if producer:
            logger.info("✓ Kafka Producer 연결 성공")
            producer.close()
            return True
        else:
            logger.error("✗ Kafka Producer 연결 실패")
            return False
    except Exception as e:
        logger.error(f"✗ Kafka 연결 테스트 실패: {e}")
        return False

def test_webdriver():
    """WebDriver 테스트"""
    logger = setup_logging()
    logger.info("=== WebDriver 테스트 시작 ===")
    
    try:
        from selenium import webdriver
        config = get_config()
        
        options = webdriver.ChromeOptions()
        if config['webdriver']['headless']:
            options.add_argument('--headless')
        options.add_argument('--no-sandbox')
        options.add_argument('--disable-dev-shm-usage')
        
        driver = webdriver.Chrome(options=options)
        driver.get("https://www.google.com")
        
        title = driver.title
        driver.quit()
        
        logger.info(f"✓ WebDriver 테스트 성공: {title}")
        return True
    except Exception as e:
        logger.error(f"✗ WebDriver 테스트 실패: {e}")
        return False

def main():
    parser = argparse.ArgumentParser(description='선수 수집기 테스트 스크립트')
    parser.add_argument('--team', choices=list(TEAM_CODE_TO_NAME.keys()), 
                       help='테스트할 팀 코드 (지정하지 않으면 전체 테스트)')
    parser.add_argument('--limit', type=int, 
                       help='테스트할 선수 수 제한')
    parser.add_argument('--no-headless', action='store_true',
                       help='헤드리스 모드 비활성화 (브라우저 창 표시)')
    parser.add_argument('--test-only', choices=['kafka', 'webdriver'],
                       help='특정 구성 요소만 테스트')
    
    args = parser.parse_args()
    
    logger = setup_logging()
    logger.info("=== 선수 수집기 테스트 시작 ===")
    
    # 특정 구성 요소만 테스트
    if args.test_only == 'kafka':
        test_kafka_connection()
        return
    elif args.test_only == 'webdriver':
        test_webdriver()
        return
    
    # 전체 구성 요소 테스트
    logger.info("1. Kafka 연결 테스트")
    if not test_kafka_connection():
        logger.error("Kafka 연결 실패로 테스트 중단")
        return
    
    logger.info("2. WebDriver 테스트")
    if not test_webdriver():
        logger.error("WebDriver 테스트 실패로 테스트 중단")
        return
    
    # 선수 수집 테스트
    if args.team:
        logger.info(f"3. {args.team} 팀 선수 수집 테스트")
        test_single_team(args.team, args.limit, args.no_headless)
    else:
        logger.info("3. 전체 선수 수집 테스트")
        try:
            # 원본 함수 실행
            scrape_all_players_and_details()
        except Exception as e:
            logger.error(f"선수 수집 테스트 실패: {e}")
            import traceback
            logger.error(traceback.format_exc())
    
    logger.info("=== 테스트 완료 ===")

if __name__ == "__main__":
    main()
