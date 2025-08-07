#!/bin/bash

echo "🚀 Tiebreaker 개발 환경 시작..."

# 기존 컨테이너 정리
echo "🧹 기존 컨테이너 정리 중..."
docker-compose -f docker-compose.dev.yml down

# 개발용 컨테이너 빌드 및 시작
echo "🔨 개발용 컨테이너 빌드 중..."
docker-compose -f docker-compose.dev.yml build

echo "▶️ 개발용 서비스 시작 중..."
docker-compose -f docker-compose.dev.yml up -d

echo "⏳ 서비스 시작 대기 중..."
sleep 10

echo "✅ 개발 환경이 시작되었습니다!"
echo "📱 프론트엔드: http://localhost:5173"
echo "🔧 API 서버: http://localhost:8080"
echo "📊 데이터 수집기: http://localhost:5001"
echo "🗄️ 데이터베이스: localhost:3306"

echo ""
echo "📝 로그 확인: docker-compose -f docker-compose.dev.yml logs -f [서비스명]"
echo "🔄 코드 변경 후 재시작: docker-compose -f docker-compose.dev.yml restart api-server"
echo "🛑 서비스 중지: docker-compose -f docker-compose.dev.yml down"

echo ""
echo "💡 개발 팁:"
echo "   - Java 코드 변경 시 자동으로 재시작됩니다 (DevTools)"
echo "   - 변경사항이 반영되지 않으면: docker-compose -f docker-compose.dev.yml restart api-server"
echo "   - 로그 확인: docker-compose -f docker-compose.dev.yml logs -f api-server" 