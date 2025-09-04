import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import axios from '../../utils/axios';

function OAuthAdditionalInfo() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login } = useAuth();

  // 폼 상태
  const [formData, setFormData] = useState({
    nickname: '',
    phone: '',
    address: ''
  });

  // UI 상태
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [userInfo, setUserInfo] = useState(null);

  // URL 파라미터에서 정보 추출
  const token = searchParams.get('token');
  const loginType = searchParams.get('loginType');
  const email = searchParams.get('email');

  useEffect(() => {
    // 토큰이 없으면 로그인 페이지로 리다이렉트
    if (!token) {
      navigate('/login', { replace: true });
      return;
    }

    // 사용자 정보 조회
    const fetchUserInfo = async () => {
      try {
        const response = await axios.get('/oauth/user-info', {
          params: { email },
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.data.success) {
          const member = response.data.member;
          setUserInfo(member);

          // 기존 정보가 있으면 폼에 미리 채우기
          setFormData({
            nickname: member.nickname || '',
            phone: member.phone || '',
            address: member.address || ''
          });
        }
      } catch (error) {
        console.error('사용자 정보 조회 오류:', error);
        setError('사용자 정보를 불러오는 중 오류가 발생했습니다.');
      }
    };

    if (email) {
      fetchUserInfo();
    }
  }, [token, email, navigate]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      // 추가 정보 업데이트 (닉네임, 전화번호, 주소)
      const updateResponse = await axios.post('/oauth/update-additional-info', null, {
        params: {
          email: email,
          nickname: formData.nickname,
          phone: formData.phone || null,
          address: formData.address || null
        },
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (updateResponse.data.success) {
        // AuthContext에 로그인 정보 저장
        login(token, userInfo?.role || 'USER', userInfo?.profileImage, formData.nickname, userInfo?.id, loginType);

        // 홈으로 리다이렉트
        navigate('/', { replace: true });
      } else {
        setError(updateResponse.data.message || '정보 저장에 실패했습니다.');
      }

    } catch (error) {
      console.error('추가 정보 저장 오류:', error);
      setError(error.response?.data?.message || '정보 저장 중 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSkip = () => {
    // 추가 정보 입력을 건너뛰고 바로 로그인
    login(token, userInfo?.role || 'USER', userInfo?.profileImage, userInfo?.nickname, userInfo?.id, loginType);
    navigate('/', { replace: true });
  };

  if (!token) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="text-center">
          <h2 className="text-3xl font-bold text-gray-900 mb-2">
            ⚾ Tiebreaker
          </h2>
          <p className="text-gray-600">추가 정보를 입력해주세요</p>
        </div>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          {/* 소셜 로그인 성공 안내 */}
          <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-green-400" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-green-800">
                  {loginType?.toUpperCase()} 로그인이 완료되었습니다!
                </p>
                <p className="text-sm text-green-700">
                  서비스를 더 편리하게 이용하기 위해 추가 정보를 입력해주세요.
                </p>
              </div>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* 닉네임 */}
            <div>
              <label htmlFor="nickname" className="block text-sm font-medium text-gray-700">
                닉네임 *
              </label>
              <div className="mt-1">
                <input
                  id="nickname"
                  name="nickname"
                  type="text"
                  required
                  value={formData.nickname}
                  onChange={handleInputChange}
                  className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                  placeholder="닉네임을 입력해주세요"
                />
              </div>
            </div>

            {/* 전화번호 */}
            <div>
              <label htmlFor="phone" className="block text-sm font-medium text-gray-700">
                전화번호
              </label>
              <div className="mt-1">
                <input
                  id="phone"
                  name="phone"
                  type="tel"
                  value={formData.phone}
                  onChange={handleInputChange}
                  className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                  placeholder="010-1234-5678"
                  pattern="[0-9]{3}-[0-9]{4}-[0-9]{4}"
                />
              </div>
              <p className="mt-1 text-sm text-gray-500">
                전화번호는 선택사항입니다. (예: 010-1234-5678)
              </p>
            </div>

            {/* 주소 */}
            <div>
              <label htmlFor="address" className="block text-sm font-medium text-gray-700">
                주소
              </label>
              <div className="mt-1">
                <input
                  id="address"
                  name="address"
                  type="text"
                  value={formData.address}
                  onChange={handleInputChange}
                  className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                  placeholder="주소를 입력해주세요"
                />
              </div>
              <p className="mt-1 text-sm text-gray-500">
                주소는 선택사항입니다.
              </p>
            </div>

            {/* 에러 메시지 */}
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md text-sm">
                {error}
              </div>
            )}

            {/* 버튼들 */}
            <div className="flex flex-col space-y-3">
              <button
                type="submit"
                disabled={isLoading || !formData.nickname.trim()}
                className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isLoading ? (
                  <div className="flex items-center">
                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    저장 중...
                  </div>
                ) : (
                  '정보 저장하고 시작하기'
                )}
              </button>

              <button
                type="button"
                onClick={handleSkip}
                disabled={isLoading}
                className="w-full flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                나중에 입력하고 시작하기
              </button>
            </div>
          </form>

          {/* 추가 안내 */}
          <div className="mt-6 text-center text-sm text-gray-500">
            <p>추가 정보는 마이페이지에서 언제든지 수정할 수 있습니다.</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default OAuthAdditionalInfo;
