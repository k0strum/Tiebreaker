import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import axios from '../../utils/axios';
import { jwtDecode } from 'jwt-decode';

function OAuthCallback() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login } = useAuth();
  const [isProcessing, setIsProcessing] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const processOAuthCallback = async () => {
      try {
        const token = searchParams.get('token');
        const loginType = searchParams.get('loginType');
        const success = searchParams.get('success');
        const errorCode = searchParams.get('errorCode');
        const errorMessage = searchParams.get('message') ? decodeURIComponent(searchParams.get('message')) : null;

        // 에러가 있는 경우
        if (success === 'false' || errorCode || errorMessage) {
          setError(errorMessage || '소셜 로그인에 실패했습니다.');
          setIsProcessing(false);
          return;
        }

        if (success === 'true' && token) {
          // JWT 토큰을 디코딩하여 사용자 정보 추출
          const decoded = jwtDecode(token);
          const email = decoded.sub; // JWT 토큰에서 이메일 추출

          // 소셜 로그인 사용자의 추가 정보 입력 필요 여부 확인
          try {
            const checkResponse = await axios.get('/oauth/check-phone', {
              params: { email },
              headers: {
                'Authorization': `Bearer ${token}`
              }
            });

            if (checkResponse.data.success) {
              const { needsPhoneInput, phone, loginType: userLoginType } = checkResponse.data;

              // 추가 정보 입력이 필요한 경우 (전화번호가 없거나 기본값인 경우)
              if (needsPhoneInput || !phone || phone === '000-0000-0000') {
                // 추가 정보 입력 페이지로 리다이렉트
                navigate(`/oauth-additional-info?token=${encodeURIComponent(token)}&loginType=${encodeURIComponent(loginType)}&email=${encodeURIComponent(email)}`, { replace: true });
                return;
              }
            }
          } catch (checkError) {
            console.warn('추가 정보 확인 중 오류 (무시하고 진행):', checkError);
            // 확인 실패 시에도 추가 정보 입력 페이지로 이동
            navigate(`/oauth-additional-info?token=${encodeURIComponent(token)}&loginType=${encodeURIComponent(loginType)}&email=${encodeURIComponent(email)}`, { replace: true });
            return;
          }

          // 추가 정보가 이미 있는 경우 바로 로그인 처리
          const response = await axios.get('/members/me', {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });

          const { role, profileImage, nickname, id: memberId } = response.data;

          // AuthContext의 login 함수 호출
          login(token, role, profileImage, nickname, memberId, loginType);

          // 로그인 성공 후 홈으로 리다이렉트
          navigate('/', { replace: true });
        } else {
          setError('소셜 로그인에 실패했습니다.');
        }
      } catch (error) {
        console.error('OAuth 콜백 처리 오류:', error);
        setError('로그인 처리 중 오류가 발생했습니다.');
      } finally {
        setIsProcessing(false);
      }
    };

    processOAuthCallback();
  }, [searchParams, navigate, login]);

  if (isProcessing) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
        <div className="sm:mx-auto sm:w-full sm:max-w-md">
          <div className="text-center">
            <h2 className="text-3xl font-bold text-gray-900 mb-2">
              ⚾ Tiebreaker
            </h2>
            <p className="text-gray-600">로그인 처리 중...</p>
          </div>
        </div>

        <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
          <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10 text-center">
            <div className="flex justify-center">
              <svg className="animate-spin h-8 w-8 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            </div>
            <p className="mt-4 text-gray-600">소셜 로그인을 처리하고 있습니다...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
        <div className="sm:mx-auto sm:w-full sm:max-w-md">
          <div className="text-center">
            <h2 className="text-3xl font-bold text-gray-900 mb-2">
              ⚾ Tiebreaker
            </h2>
            <p className="text-gray-600">로그인 오류</p>
          </div>
        </div>

        <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
          <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
            {/* 에러 아이콘 */}
            <div className="flex justify-center mb-4">
              <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center">
                <svg className="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
                </svg>
              </div>
            </div>

            {/* 에러 메시지 */}
            <div className="text-center mb-6">
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                소셜 로그인 실패
              </h3>
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md text-sm">
                {error}
              </div>
            </div>

            {/* 액션 버튼들 */}
            <div className="flex flex-col space-y-3">
              <button
                onClick={() => navigate('/login', { replace: true })}
                className="w-full bg-gray-100 hover:bg-gray-200 text-black px-6 py-3 rounded-lg transition-colors font-semibold shadow-md focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 border border-gray-300"
              >
                로그인 페이지로 돌아가기
              </button>
              <button
                onClick={() => navigate('/', { replace: true })}
                className="w-full bg-gray-100 hover:bg-gray-200 text-black px-6 py-3 rounded-lg transition-colors font-semibold shadow-md focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 border border-gray-300"
              >
                홈으로 돌아가기
              </button>
            </div>

            {/* 추가 안내 */}
            <div className="mt-6 text-center text-sm text-gray-500">
              <p>계정이 이미 존재하는 경우 일반 로그인을 이용해주세요.</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return null;
}

export default OAuthCallback;
