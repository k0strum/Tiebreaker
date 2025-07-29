import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: '/api',           // 모든 요청의 기본 URL
  withCredentials: true,      // 쿠키 포함 (소셜 로그인 필수)
});

// 요청 시 토큰 자동 추가 (옵션: 로그인 구현된 경우)
axiosInstance.interceptors.request.use((config) => {
  const token =
    localStorage.getItem('token') || localStorage.getItem('accessToken');
  if (token) {
    if (config.headers && typeof config.headers.set === 'function') {
      // AxiosHeaders 객체일 때
      config.headers.set('Authorization', `Bearer ${token}`);
    } else {
      // 일반 객체일 때
      config.headers = config.headers || {};
      config.headers['Authorization'] = `Bearer ${token}`;
    }
  }
  if (config.data instanceof FormData) {
    if (config.headers && typeof config.headers.delete === 'function') {
      config.headers.delete('Content-Type');
    } else if (config.headers) {
      delete config.headers['Content-Type'];
    }
  }
  console.log('📡 axios 요청:', config.method?.toUpperCase(), config.url, config);
  return config;
});

// 응답 시 에러 처리 공통화
axiosInstance.interceptors.response.use(
  (res) => {
    // 로그인 응답일 때 토큰 자동 저장
    if (
      res.config.url &&
      (res.config.url.endsWith('/members/login') ||
        res.config.url.endsWith('/api/members/login') ||
        res.config.url.endsWith('/auth/oauth2/kakao') ||
        res.config.url.endsWith('/auth/oauth2/google') ||
        res.config.url.endsWith('/auth/oauth2/naver'))
    ) {
      if (res.data && res.data.token) {
        localStorage.setItem('token', res.data.token);
      }
      if (res.data && res.data.accessToken) {
        localStorage.setItem('accessToken', res.data.accessToken);
      }
      if (res.data && res.data.refreshToken) {
        localStorage.setItem('refreshToken', res.data.refreshToken);
      }
      // 사용자 정보도 저장
      if (res.data && res.data.user) {
        localStorage.setItem('user', JSON.stringify(res.data.user));
      }
    }
    return res;
  },
  async (err) => {
    console.error('[Axios Error]', err.response || err);

    // 401 에러 (토큰 만료) 처리
    if (err.response?.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken');
      
      if (refreshToken) {
        try {
          // 리프레시 토큰으로 새 토큰 발급 시도
          const refreshResponse = await axios.post('/api/auth/refresh', {
            refreshToken: refreshToken
          });
          
          if (refreshResponse.data.accessToken) {
            localStorage.setItem('accessToken', refreshResponse.data.accessToken);
            
            // 원래 요청 재시도
            const originalRequest = err.config;
            originalRequest.headers['Authorization'] = `Bearer ${refreshResponse.data.accessToken}`;
            return axiosInstance(originalRequest);
          }
        } catch (refreshError) {
          console.error('토큰 갱신 실패:', refreshError);
        }
      }
      
      // 토큰 갱신 실패 시 로그아웃 처리
      localStorage.removeItem('token');
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      
      // 로그인 페이지로 리다이렉트
      window.location.href = '/login';
      return Promise.reject(err);
    }

    // 서버에서 보낸 에러 메시지가 있으면 그걸 사용
    if (err.response?.data?.message) {
      console.error(err.response.data.message);
    } else {
      console.error('요청 처리 중 오류가 발생했습니다.');
    }

    return Promise.reject(err);
  }
);

// 유틸리티 함수들
export const authAPI = {
  // 로그인 상태 확인
  isLoggedIn: () => {
    return !!(localStorage.getItem('token') || localStorage.getItem('accessToken'));
  },
  
  // 사용자 정보 가져오기
  getUser: () => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },
  
  // 로그아웃
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    window.location.href = '/';
  },
  
  // 토큰 가져오기
  getToken: () => {
    return localStorage.getItem('token') || localStorage.getItem('accessToken');
  }
};

export default axiosInstance; 