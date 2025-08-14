# 1. 베이스 이미지: Python 3.11 슬림 버전
FROM python:3.11-slim

# 2. 시스템 환경 변수 설정
ENV DEBIAN_FRONTEND=noninteractive
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1

# 3. 시스템 패키지 업데이트 및 Chrome 실행에 필요한 모든 의존성 라이브러리 설치
RUN apt-get update && apt-get install -y \
  wget \
  gnupg \
  ca-certificates \
  unzip \
  # --- [개선] 한글 및 CJK 폰트 깨짐 방지 ---
  fonts-noto-cjk \
  # --- Chrome 필수 라이브러리 (중복 제거 및 정리) ---
  libasound2 \
  libatk-bridge2.0-0 \
  libatk1.0-0 \
  libcairo2 \
  libcups2 \
  libdbus-1-3 \
  libdrm2 \
  libexpat1 \
  libfontconfig1 \
  libgbm1 \
  libglib2.0-0 \
  libgtk-3-0 \
  libnspr4 \
  libnss3 \
  libpango-1.0-0 \
  libx11-6 \
  libx11-xcb1 \
  libxcb1 \
  libxcomposite1 \
  libxcursor1 \
  libxdamage1 \
  libxext6 \
  libxfixes3 \
  libxi6 \
  libxrandr2 \
  libxrender1 \
  libxss1 \
  libxtst6 \
  lsb-release \
  xdg-utils \
  && rm -rf /var/lib/apt/lists/*

# 4. Google Chrome 설치 (Selenium Manager로 드라이버 자동 관리)
RUN mkdir -p /usr/share/keyrings \
  && wget -qO- https://dl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /usr/share/keyrings/google-chrome.gpg \
  && echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-chrome.gpg] https://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list \
  && apt-get update \
  && apt-get install -y google-chrome-stable \
  && rm -rf /var/lib/apt/lists/*

# 5. [개선] 비-루트(Non-Root) 사용자 생성
RUN groupadd -r appgroup && useradd -r -g appgroup -d /app -s /sbin/nologin -c "Application User" appuser

# 6. 파이썬 애플리케이션 설정
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# requirements.txt에 gunicorn이 포함되어 있어야 합니다.
# gunicorn==22.0.0

COPY . .
# [개선] 생성한 사용자가 앱 파일을 소유하도록 변경
RUN chown -R appuser:appgroup /app

# 7. [개선] 생성한 사용자로 전환
USER appuser

# 8. [개선] Gunicorn으로 애플리케이션 실행
# app:app -> app.py 파일 안의 app = Flask(__name__) 변수를 의미합니다.
# workers: 동시에 처리할 수 있는 프로세스 수 (스케줄러 중복 실행 방지를 위해 1로 설정)
CMD ["gunicorn", "--bind", "0.0.0.0:5000", "--workers", "1", "app:app"]