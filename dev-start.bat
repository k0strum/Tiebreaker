@echo off
chcp 65001 >nul
echo 🚀 Tiebreaker 개발 환경 시작...

REM 기존 컨테이너 정리
echo 🧹 기존 컨테이너 & 볼륨 정리 중...
docker-compose -f docker-compose.dev.yml down --volumes
if %errorlevel% neq 0 (
    echo ❌ 컨테이너 정리 중 오류가 발생했습니다.
    pause
    exit /b 1
)

REM Docker 이미지 정리 (선택사항)
echo 🗑️ 사용하지 않는 이미지 정리 중...
docker system prune -f

REM 개발용 컨테이너 빌드 및 시작
echo 🔨 개발용 컨테이너 빌드 중...
docker-compose -f docker-compose.dev.yml build
if %errorlevel% neq 0 (
    echo ❌ 컨테이너 빌드 중 오류가 발생했습니다.
    pause
    exit /b 1
)

echo ▶️ 개발용 서비스 시작 중...
docker-compose -f docker-compose.dev.yml up -d
if %errorlevel% neq 0 (
    echo ❌ 서비스 시작 중 오류가 발생했습니다.
    pause
    exit /b 1
)

REM 서비스 시작 대기 (더 긴 시간)
echo ⏳ 서비스 시작 대기 중... (최대 60초)
timeout /t 15 /nobreak >nul

REM Health Check 확인
echo 🔍 서비스 상태 확인 중...
docker-compose -f docker-compose.dev.yml ps

REM Health Check 검증
echo 🔬 Health Check 검증 중...
set /a attempts=0
:health_check_loop
set /a attempts+=1
if %attempts% gtr 12 (
    echo ⚠️ 일부 서비스가 아직 준비되지 않았습니다. 수동으로 확인해주세요.
    goto :show_info
)

docker-compose -f docker-compose.dev.yml ps | findstr "unhealthy" >nul
if %errorlevel% equ 0 (
    echo ⏳ 서비스가 아직 준비 중입니다... (%attempts%/12)
    timeout /t 5 /nobreak >nul
    goto :health_check_loop
)

echo ✅ 모든 서비스가 정상적으로 시작되었습니다!

:show_info
echo.
echo 🎉 개발 환경이 시작되었습니다!
echo 📱 프론트엔드: http://localhost:5173
echo 🔧 API 서버: http://localhost:8080
echo 📊 데이터 수집기: http://localhost:5001
echo 🗄️ 데이터베이스: localhost:3307
echo 🔍 Actuator Health: http://localhost:8080/actuator/health

echo.
echo 📝 유용한 명령어:
echo   로그 확인: docker-compose -f docker-compose.dev.yml logs -f [서비스명]
echo   서비스 중지: docker-compose -f docker-compose.dev.yml down
echo   서비스 재시작: docker-compose -f docker-compose.dev.yml restart [서비스명]
echo   전체 재빌드: docker-compose -f docker-compose.dev.yml build --no-cache

echo.
echo 💡 문제 해결 팁:
echo   - SIGTERM 에러: docker-compose -f docker-compose.dev.yml restart frontend
echo   - API 서버 문제: docker-compose -f docker-compose.dev.yml logs api-server
echo   - DB 연결 문제: docker-compose -f docker-compose.dev.yml logs db

echo.
pause 