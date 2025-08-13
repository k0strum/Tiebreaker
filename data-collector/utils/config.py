# 도커 컨테이너 환경 설정 (Linux)
CONFIG = {
    'chrome_path': '/usr/bin/google-chrome',  # Dockerfile에서 설치한 Chrome 경로
    'chromedriver_path': None,  # Selenium Manager가 자동 관리
    'profile_path': None,
    'profile_directory': None,
    'headless': True,  # 컨테이너에서는 headless 필수
    'use_profile': False
}

def get_config():
    """도커 환경 설정을 반환합니다."""
    return CONFIG
