import React, { useState, useEffect, useRef } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import axios from '../../utils/axios';

function EmailVerification() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState('verifying'); // 'verifying', 'success', 'error'
  const [message, setMessage] = useState('');

  const effectRan = useRef(false);

  useEffect(() => {
    // 👇 if 문으로 전체 로직을 감싸줍니다.
    if (effectRan.current === false) {
      const token = searchParams.get('token');
      const type = searchParams.get('type');

      if (!token) {
        setStatus('error');
        setMessage('유효하지 않은 인증 링크입니다.');
        return;
      }

      verifyEmail(token, type);

      // 👇 정리(cleanup) 함수에서 플래그를 true로 설정합니다.
      return () => {
        effectRan.current = true;
      };
    }
  }, []); // 의존성 배열은 그대로 []를 유지합니다.

  const verifyEmail = async (token, type) => {
    try {
      const response = await axios.get(`/email/verify?token=${token}`);
      
      if (response.status === 200) {
        setStatus('success');
        setMessage('이메일 인증이 완료되었습니다!');
      } else {
        setStatus('error');
        setMessage('인증 처리 중 오류가 발생했습니다.');
      }
    } catch (error) {
      console.error('이메일 인증 실패:', error);
      setStatus('error');
      
      if (error.response?.data) {
        setMessage(error.response.data.message || '오류가 발생했습니다.');
      } else {
        setMessage('인증 처리 중 오류가 발생했습니다.');
      }
    }
  };

  const handleGoToLogin = () => {
    navigate('/login');
  };

  const handleGoToHome = () => {
    navigate('/');
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="text-center">
          <h2 className="text-3xl font-bold text-gray-900 mb-2">
            ⚾ Tiebreaker
          </h2>
          <p className="text-gray-600">이메일 인증</p>
        </div>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          <div className="text-center">
            {status === 'verifying' && (
              <>
                <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-blue-100 mb-4">
                  <svg className="animate-spin h-6 w-6 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                  이메일 인증 중...
                </h3>
                <p className="text-sm text-gray-600">
                  잠시만 기다려주세요.
                </p>
              </>
            )}

            {status === 'success' && (
              <>
                <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100 mb-4">
                  <svg className="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                  인증 완료!
                </h3>
                <p className="text-sm text-gray-600 mb-6">
                  {message}
                </p>
                <div className="space-y-3">
                  <button
                    onClick={handleGoToLogin}
                    className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                  >
                    로그인하기
                  </button>
                  <button
                    onClick={handleGoToHome}
                    className="w-full flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                  >
                    홈으로 이동
                  </button>
                </div>
              </>
            )}

            {status === 'error' && (
              <>
                <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 mb-4">
                  <svg className="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                  인증 실패
                </h3>
                <p className="text-sm text-gray-600 mb-6">
                  {message}
                </p>
                <div className="space-y-3">
                  <button
                    onClick={handleGoToLogin}
                    className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                  >
                    로그인 페이지로 이동
                  </button>
                  <button
                    onClick={handleGoToHome}
                    className="w-full flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                  >
                    홈으로 이동
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default EmailVerification; 