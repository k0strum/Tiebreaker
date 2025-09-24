#!/bin/bash

# 선수 수집기 테스트 실행 스크립트

echo "=== 선수 수집기 테스트 스크립트 ==="
echo "사용법:"
echo "  ./run_test.sh                    # 전체 테스트"
echo "  ./run_test.sh kafka              # Kafka 연결 테스트만"
echo "  ./run_test.sh webdriver          # WebDriver 테스트만"
echo "  ./run_test.sh team SK            # SK 팀만 테스트"
echo "  ./run_test.sh team SK 5          # SK 팀에서 5명만 테스트"
echo "  ./run_test.sh team SK 5 nohead   # SK 팀에서 5명 테스트 (브라우저 창 표시)"
echo ""

# 컨테이너 내부에서 실행
if [ "$1" = "kafka" ]; then
    echo "Kafka 연결 테스트 실행 중..."
    docker compose exec data-collector python test_player_collector.py --test-only kafka
elif [ "$1" = "webdriver" ]; then
    echo "WebDriver 테스트 실행 중..."
    docker compose exec data-collector python test_player_collector.py --test-only webdriver
elif [ "$1" = "team" ]; then
    TEAM=${2:-SK}
    LIMIT=${3:-}
    NOHEADLESS=${4:-}
    
    if [ "$NOHEADLESS" = "nohead" ]; then
        echo "${TEAM} 팀 테스트 실행 중 (브라우저 창 표시)..."
        docker compose exec data-collector python test_player_collector.py --team $TEAM --limit $LIMIT --no-headless
    else
        echo "${TEAM} 팀 테스트 실행 중..."
        docker compose exec data-collector python test_player_collector.py --team $TEAM --limit $LIMIT
    fi
else
    echo "전체 테스트 실행 중..."
    docker compose exec data-collector python test_player_collector.py
fi
