# Python 3.11 버전의 가벼운 이미지를 기반으로 사용
FROM python:3.11-slim

# 컨테이너 내 작업 디렉터리 설정
WORKDIR /app

# 라이브러리 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 소스코드 복사
COPY . .

# 컨테이너 시작 시 실행할 명령어
CMD ["flask", "run", "--host=0.0.0.0"]