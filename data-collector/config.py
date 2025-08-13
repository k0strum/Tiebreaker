import os

# 환경 설정
ENVIRONMENT = os.getenv('ENVIRONMENT', 'local')  # 'local' 또는 'docker'

# 로컬 환경 설정 (Windows)
LOCAL_CONFIG = {
    'chrome_path': r"C:\Kostrum\Tiebreaker\data-collector\chrome-win64\chrome.exe",
    'chromedriver_path': r"C:\Kostrum\Tiebreaker\data-collector\chromedriver-win64\chromedriver.exe",
    'profile_path': r"C:\Users\TJ-BU-707-P14\AppData\Local\Google\Chrome\User Data",
    'profile_directory': 'Profile 1',
    'headless': False,
    'use_profile': True
}

# 도커 컨테이너 환경 설정 (Linux)
DOCKER_CONFIG = {
    'chrome_path': '/usr/bin/google-chrome',  # Dockerfile에서 설치한 Chrome 경로
    'chromedriver_path': '/usr/local/bin/chromedriver',  # Dockerfile에서 설치한 ChromeDriver 경로
    'profile_path': None,
    'profile_directory': None,
    'headless': True,  # 컨테이너에서는 headless 필수
    'use_profile': False
}

def get_config():
    """현재 환경에 맞는 설정을 반환합니다."""
    if os.getenv('ENVIRONMENT') == 'docker':
        return DOCKER_CONFIG
    else:
        return LOCAL_CONFIG

def is_docker():
    """도커 환경인지 확인합니다."""
    return ENVIRONMENT == 'docker'

def is_local():
    """로컬 환경인지 확인합니다."""
    return ENVIRONMENT == 'local'
