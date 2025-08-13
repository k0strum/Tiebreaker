# 🚀 선수 데이터 수집 실행 가이드

## 📋 환경별 실행 방법

### 🖥️ **로컬 환경에서 실행 (Windows)**

#### 1. 가상환경 활성화

```bash
cd data-collector
venv\Scripts\activate
```

#### 2. 의존성 설치

```bash
pip install -r requirements.txt
```

#### 3. Chrome 127 버전 확인

- `chrome-win64\chrome.exe` 파일이 존재하는지 확인
- `chromedriver-win64\chromedriver.exe` 파일이 존재하는지 확인

#### 4. 실행

```bash
python run_local.py
```

### 🐳 **도커 컨테이너에서 실행**

#### 0. Chrome 및 ChromeDriver ZIP 파일 준비

- `data-collector/third_party/` 폴더에 다음 파일들이 있어야 합니다:
  - `chrome-linux64.zip` (Chrome 127.0.6533.72)
  - `chromedriver-linux64.zip` (ChromeDriver 127.0.6533.72)

#### 1. 컨테이너 재빌드 (로컬 ZIP 파일 사용)

```bash
docker-compose -f docker-compose.dev.yml build data-collector
```

#### 2. 컨테이너 시작

```bash
docker-compose -f docker-compose.dev.yml up -d
```

#### 3. 컨테이너 내에서 실행

```bash
docker exec -it tiebreaker-data-collector-1 python run_docker.py
```

## 🔧 **환경별 설정 차이점**

| 설정              | 로컬 환경                             | 도커 환경     |
| ----------------- | ------------------------------------- | ------------- |
| Chrome 경로       | `chrome-win64\chrome.exe`             | 시스템 기본   |
| ChromeDriver 경로 | `chromedriver-win64\chromedriver.exe` | 시스템 기본   |
| Headless 모드     | ❌ (GUI 표시)                         | ✅ (필수)     |
| 프로필 사용       | ✅                                    | ❌            |
| Chrome 버전       | 127.0.6533.72                         | 127.0.6533.72 |

## 📊 **실행 결과 확인**

### Kafka 토픽 확인

```bash
# Kafka 컨테이너에 접속
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic kbo-player-data --from-beginning
```

### Spring Boot 로그 확인

```bash
# API 서버 로그 확인
docker-compose -f docker-compose.dev.yml logs -f api-server
```

### 데이터베이스 확인

```bash
# MySQL 컨테이너에 접속
docker exec -it tiebreak-db-dev mysql -u user -p tiebreak_db
```

## 🚨 **문제 해결**

### 로컬 환경 문제

1. **Chrome 경로 오류**: `config.py`에서 경로 확인
2. **ChromeDriver 버전 불일치**: Chrome 127 버전과 맞는 ChromeDriver 사용
3. **프로필 로그인 필요**: Chrome에서 수동으로 로그인 후 실행

### 도커 환경 문제

1. **Chrome 설치 실패**: Dockerfile에서 Chrome 127 버전 설치 확인
2. **권한 문제**: `chmod +x /usr/local/bin/chromedriver` 실행
3. **메모리 부족**: Docker 메모리 할당량 증가

## 📝 **로그 확인**

### 로컬 환경 로그

- 콘솔에 직접 출력
- `kbo_players.json` 파일 생성

### 도커 환경 로그

```bash
# 실시간 로그 확인
docker-compose -f docker-compose.dev.yml logs -f data-collector

# 특정 컨테이너 로그
docker logs tiebreaker-data-collector-1
```
