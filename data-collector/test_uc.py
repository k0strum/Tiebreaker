import undetected_chromedriver as uc
import time

print("undetected-chromedriver 프로필 테스트를 시작합니다...")
driver = None
try:
    options = uc.ChromeOptions()
    
    # --- 실제 프로필을 사용하는 옵션 추가 ---
    profile_path = r"C:\Users\TJ-BU-707-P14\AppData\Local\Google\Chrome\User Data"
    options.add_argument(f'--user-data-dir={profile_path}')
    
    driver = uc.Chrome(options=options)
    print("프로필을 사용하여 브라우저를 성공적으로 실행했습니다.")
    
    driver.get("https://www.google.com")
    print(f"현재 페이지 제목: {driver.title}")
    time.sleep(5)
    print("테스트 성공!")

except Exception as e:
    print(f"테스트 중 오류 발생: {e}")

finally:
    if driver:
        driver.quit()
    print("테스트를 종료합니다.")