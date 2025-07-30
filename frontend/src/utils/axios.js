import axios from 'axios';

/**
 * axios 인스턴스 생성 - 백엔드 API와의 통신을 위한 설정
 * 
 * 주요 역할:
 * 1. 모든 API 요청의 기본 설정 관리
 * 2. JWT 토큰 자동 첨부
 * 3. 로그인 시 토큰 자동 저장
 * 4. 에러 처리 공통화
 */
const axiosInstance = axios.create({
  // 모든 API 요청의 기본 URL
  // React 개발 서버의 proxy 설정을 통해 Spring Boot 서버로 연결됨
  // 예: /api/members/login -> http://localhost:8080/api/members/login
  baseURL: '/api',
  
  // 쿠키를 포함한 요청 허용 (소셜 로그인 등에서 필요)
  withCredentials: true,
});

/**
 * 요청 인터셉터 - 모든 API 요청이 서버로 가기 전에 실행
 * 
 * 주요 기능:
 * 1. JWT 토큰 자동 첨부
 * 2. FormData 처리 시 Content-Type 헤더 제거
 * 3. 요청 로깅
 */
axiosInstance.interceptors.request.use((config) => {
  // localStorage에서 JWT 토큰 가져오기
  // 'token' 또는 'accessToken' 키로 저장된 토큰 사용
  const token = localStorage.getItem('token') || localStorage.getItem('accessToken');
  
  if (token) {
    // 토큰이 존재하면 Authorization 헤더에 Bearer 토큰으로 추가
    if (config.headers && typeof config.headers.set === 'function') {
      // AxiosHeaders 객체일 때 (최신 버전)
      config.headers.set('Authorization', `Bearer ${token}`);
    } else {
      // 일반 객체일 때 (구버전 호환)
      config.headers = config.headers || {};
      config.headers['Authorization'] = `Bearer ${token}`;
    }
  }
  
  // FormData를 사용하는 경우 (파일 업로드 등)
  // 브라우저가 자동으로 Content-Type을 설정하도록 헤더 제거
  if (config.data instanceof FormData) {
    if (config.headers && typeof config.headers.delete === 'function') {
      config.headers.delete('Content-Type');
    } else if (config.headers) {
      delete config.headers['Content-Type'];
    }
  }
  
  // 개발용 요청 로깅 (프로덕션에서는 제거 가능)
  console.log('📡 axios 요청:', config.method?.toUpperCase(), config.url, config);
  return config;
});

/**
 * 응답 인터셉터 - 서버로부터 응답을 받은 후 실행
 * 
 * 주요 기능:
 * 1. 로그인 성공 시 토큰 자동 저장
 * 2. 에러 응답 공통 처리
 * 3. 에러 메시지 로깅
 */
axiosInstance.interceptors.response.use(
  // 성공 응답 처리
  (res) => {
    // 로그인 API 응답인지 확인
    if (
      res.config.url &&
      (res.config.url.endsWith('/members/login') ||
        res.config.url.endsWith('/api/members/login'))
    ) {
      // 로그인 성공 시 서버에서 받은 JWT 토큰을 localStorage에 저장
      if (res.data && res.data.token) {
        localStorage.setItem('token', res.data.token);
        console.log('✅ 로그인 토큰 자동 저장 완료');
      }
    }
    return res;
  },
  
  // 에러 응답 처리
  (err) => {
    // 전체 에러 정보 로깅
    console.error('[Axios Error]', err.response || err);

    // 서버에서 보낸 구체적인 에러 메시지가 있으면 사용
    if (err.response?.data?.message) {
      console.error('서버 에러 메시지:', err.response.data.message);
    } else {
      // 서버 에러 메시지가 없으면 기본 메시지 사용
      console.error('요청 처리 중 오류가 발생했습니다.');
    }

    // 에러를 다시 던져서 컴포넌트에서 catch할 수 있도록 함
    return Promise.reject(err);
  }
);

export default axiosInstance; 