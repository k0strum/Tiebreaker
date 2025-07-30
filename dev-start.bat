@echo off
chcp 65001 >nul
echo 🚀 Tiebreaker 개발 환경 시작...

REM 기존 컨테이너 정리
echo 🧹 기존 컨테이너 정리 중...
docker-compose -f docker-compose.dev.yml down

REM Docker 이미지 정리 (선택사항)
echo 🗑️ 사용하지 않는 이미지 정리 중...
docker system prune -f

REM 개발용 컨테이너 빌드 및 시작
echo 🔨 개발용 컨테이너 빌드 중...
docker-compose -f docker-compose.dev.yml build --no-cache

echo ▶️ 개발용 서비스 시작 중...
docker-compose -f docker-compose.dev.yml up -d

REM 서비스 시작 대기
echo ⏳ 서비스 시작 대기 중...
timeout /t 10 /nobreak >nul

REM 서비스 상태 확인
echo 🔍 서비스 상태 확인 중...
docker-compose -f docker-compose.dev.yml ps

echo.
echo ✅ 개발 환경이 시작되었습니다!
echo 📱 프론트엔드: http://localhost:5173
echo 🔧 API 서버: http://localhost:8080
echo 📊 데이터 수집기: http://localhost:5001
echo 🗄️ 데이터베이스: localhost:3307

echo.
echo 📝 로그 확인: docker-compose -f docker-compose.dev.yml logs -f [서비스명]
echo 🛑 서비스 중지: docker-compose -f docker-compose.dev.yml down
echo 🔄 서비스 재시작: docker-compose -f docker-compose.dev.yml restart [서비스명]

echo.
echo 💡 팁: SIGTERM 에러가 발생하면 다음 명령어로 재시작하세요:
echo    docker-compose -f docker-compose.dev.yml restart frontend

pause 