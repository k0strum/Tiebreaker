import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import axios from '../../utils/axios';

function Signup() {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    phone: '',
    address: ''
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [validationErrors, setValidationErrors] = useState({});

  const { isLoggedIn } = useAuth();
  const navigate = useNavigate();

  // 이미 로그인된 경우 홈으로 리다이렉트
  useEffect(() => {
    if (isLoggedIn) {
      navigate('/');
    }
  }, [isLoggedIn, navigate]);

  // 폼 데이터 변경 처리
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // 실시간 유효성 검사
    validateField(name, value);
  };

  // 필드별 유효성 검사
  const validateField = (name, value) => {
    const errors = { ...validationErrors };
    
    switch (name) {
      case 'email':
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!value) {
          errors.email = '이메일을 입력해주세요.';
        } else if (!emailRegex.test(value)) {
          errors.email = '올바른 이메일 형식을 입력해주세요.';
        } else {
          delete errors.email;
        }
        break;
        
      case 'password':
        if (!value) {
          errors.password = '비밀번호를 입력해주세요.';
        } else if (value.length < 8) {
          errors.password = '비밀번호는 8자 이상이어야 합니다.';
        } else if (!/(?=.*[a-zA-Z])(?=.*[0-9])/.test(value)) {
          errors.password = '비밀번호는 영문과 숫자를 포함해야 합니다.';
        } else {
          delete errors.password;
        }
        
        // 비밀번호 확인도 함께 검사
        if (formData.confirmPassword && value !== formData.confirmPassword) {
          errors.confirmPassword = '비밀번호가 일치하지 않습니다.';
        } else if (formData.confirmPassword) {
          delete errors.confirmPassword;
        }
        break;
        
      case 'confirmPassword':
        if (!value) {
          errors.confirmPassword = '비밀번호 확인을 입력해주세요.';
        } else if (value !== formData.password) {
          errors.confirmPassword = '비밀번호가 일치하지 않습니다.';
        } else {
          delete errors.confirmPassword;
        }
        break;
        
      case 'nickname':
        if (!value) {
          errors.nickname = '닉네임을 입력해주세요.';
        } else if (value.length < 2) {
          errors.nickname = '닉네임은 2자 이상이어야 합니다.';
        } else if (value.length > 20) {
          errors.nickname = '닉네임은 20자 이하여야 합니다.';
        } else {
          delete errors.nickname;
        }
        break;
        
      case 'phone':
        if (value && !/^[0-9-]+$/.test(value)) {
          errors.phone = '올바른 전화번호 형식을 입력해주세요.';
        } else {
          delete errors.phone;
        }
        break;
        
      default:
        break;
    }
    
    setValidationErrors(errors);
  };

  // 전체 폼 유효성 검사
  const validateForm = () => {
    const errors = {};
    
    if (!formData.email) errors.email = '이메일을 입력해주세요.';
    if (!formData.password) errors.password = '비밀번호를 입력해주세요.';
    if (!formData.confirmPassword) errors.confirmPassword = '비밀번호 확인을 입력해주세요.';
    if (!formData.nickname) errors.nickname = '닉네임을 입력해주세요.';
    
    if (formData.password !== formData.confirmPassword) {
      errors.confirmPassword = '비밀번호가 일치하지 않습니다.';
    }
    
    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // 회원가입 폼 제출 처리
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      setError('입력 정보를 확인해주세요.');
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      // 백엔드 API에 전송할 데이터 (MemberCreateRequest 구조에 맞춤)
      const signupData = {
        email: formData.email,
        password: formData.password,
        nickname: formData.nickname,
        phone: formData.phone || null,
        address: formData.address || null
      };

      const response = await axios.post('/members/join', signupData);
      
      console.log('회원가입 성공:', response.data);
      
       // 회원가입 성공 후 로그인 페이지로 이동
       // TODO: 이메일 인증 기능 추가 예정
       alert('회원가입이 완료되었습니다! 로그인해주세요.');
       navigate('/login');
      
    } catch (error) {
      console.error('회원가입 오류:', error);
      
      if (error.response?.status === 400) {
        if (error.response.data?.message) {
          setError(error.response.data.message);
        } else {
          setError('입력 정보를 확인해주세요.');
        }
      } else if (error.response?.status === 409) {
        setError('이미 사용 중인 이메일입니다.');
      } else if (error.response?.data?.message) {
        setError(error.response.data.message);
      } else {
        setError('회원가입 중 오류가 발생했습니다. 다시 시도해주세요.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="text-center">
          <h2 className="text-3xl font-bold text-gray-900 mb-2">
            ⚾ Tiebreaker
          </h2>
          <p className="text-gray-600">KBO 팬 플랫폼에 가입하세요</p>
        </div>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          <form className="space-y-6" onSubmit={handleSubmit}>
            {/* 이메일 입력 필드 */}
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                이메일 주소 <span className="text-red-500">*</span>
              </label>
              <div className="mt-1">
                <input
                  id="email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  required
                  value={formData.email}
                  onChange={handleInputChange}
                  className={`appearance-none block w-full px-3 py-2 border rounded-md placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm ${
                    validationErrors.email ? 'border-red-300' : 'border-gray-300'
                  }`}
                  placeholder="example@email.com"
                />
                {validationErrors.email && (
                  <p className="mt-1 text-sm text-red-600">{validationErrors.email}</p>
                )}
              </div>
            </div>

            {/* 비밀번호 입력 필드 */}
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                비밀번호 <span className="text-red-500">*</span>
              </label>
              <div className="mt-1 relative">
                <input
                  id="password"
                  name="password"
                  type={showPassword ? "text" : "password"}
                  autoComplete="new-password"
                  required
                  value={formData.password}
                  onChange={handleInputChange}
                  className={`appearance-none block w-full px-3 py-2 pr-10 border rounded-md placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm ${
                    validationErrors.password ? 'border-red-300' : 'border-gray-300'
                  }`}
                  placeholder="8자 이상, 영문+숫자 포함"
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-0 pr-3 flex items-center"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? (
                    <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21" />
                    </svg>
                  ) : (
                    <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                  )}
                </button>
                {validationErrors.password && (
                  <p className="mt-1 text-sm text-red-600">{validationErrors.password}</p>
                )}
              </div>
            </div>

            {/* 비밀번호 확인 입력 필드 */}
            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700">
                비밀번호 확인 <span className="text-red-500">*</span>
              </label>
              <div className="mt-1 relative">
                <input
                  id="confirmPassword"
                  name="confirmPassword"
                  type={showConfirmPassword ? "text" : "password"}
                  autoComplete="new-password"
                  required
                  value={formData.confirmPassword}
                  onChange={handleInputChange}
                  className={`appearance-none block w-full px-3 py-2 pr-10 border rounded-md placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm ${
                    validationErrors.confirmPassword ? 'border-red-300' : 'border-gray-300'
                  }`}
                  placeholder="비밀번호를 다시 입력해주세요"
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-0 pr-3 flex items-center"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                >
                  {showConfirmPassword ? (
                    <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21" />
                    </svg>
                  ) : (
                    <svg className="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                  )}
                </button>
                {validationErrors.confirmPassword && (
                  <p className="mt-1 text-sm text-red-600">{validationErrors.confirmPassword}</p>
                )}
              </div>
            </div>

            {/* 닉네임 입력 필드 */}
            <div>
              <label htmlFor="nickname" className="block text-sm font-medium text-gray-700">
                닉네임 <span className="text-red-500">*</span>
              </label>
              <div className="mt-1">
                <input
                  id="nickname"
                  name="nickname"
                  type="text"
                  autoComplete="nickname"
                  required
                  value={formData.nickname}
                  onChange={handleInputChange}
                  className={`appearance-none block w-full px-3 py-2 border rounded-md placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm ${
                    validationErrors.nickname ? 'border-red-300' : 'border-gray-300'
                  }`}
                  placeholder="2-20자 사이의 닉네임"
                />
                {validationErrors.nickname && (
                  <p className="mt-1 text-sm text-red-600">{validationErrors.nickname}</p>
                )}
              </div>
            </div>

            {/* 전화번호 입력 필드 (선택사항) */}
            <div>
              <label htmlFor="phone" className="block text-sm font-medium text-gray-700">
                전화번호 <span className="text-gray-500">(선택)</span>
              </label>
              <div className="mt-1">
                <input
                  id="phone"
                  name="phone"
                  type="tel"
                  autoComplete="tel"
                  value={formData.phone}
                  onChange={handleInputChange}
                  className={`appearance-none block w-full px-3 py-2 border rounded-md placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm ${
                    validationErrors.phone ? 'border-red-300' : 'border-gray-300'
                  }`}
                  placeholder="010-1234-5678"
                />
                {validationErrors.phone && (
                  <p className="mt-1 text-sm text-red-600">{validationErrors.phone}</p>
                )}
              </div>
            </div>

                         {/* 주소 입력 필드 (선택사항) */}
             <div>
               <label htmlFor="address" className="block text-sm font-medium text-gray-700">
                 주소 <span className="text-gray-500">(선택)</span>
               </label>
               <div className="mt-1">
                 <input
                   id="address"
                   name="address"
                   type="text"
                   autoComplete="street-address"
                   value={formData.address}
                   onChange={handleInputChange}
                   className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                   placeholder="서울시 강남구..."
                 />
               </div>
             </div>

             {/* 이메일 인증 필드 (향후 구현 예정) */}
             {/* 
             <div>
               <label htmlFor="emailVerification" className="block text-sm font-medium text-gray-700">
                 이메일 인증 <span className="text-red-500">*</span>
               </label>
               <div className="mt-1 flex space-x-2">
                 <input
                   id="emailVerification"
                   name="emailVerification"
                   type="text"
                   className="flex-1 appearance-none block px-3 py-2 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                   placeholder="인증번호 6자리"
                   maxLength="6"
                 />
                 <button
                   type="button"
                   className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                 >
                   인증번호 발송
                 </button>
               </div>
             </div>
             */}

            {/* 에러 메시지 */}
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md text-sm">
                {error}
              </div>
            )}

            {/* 회원가입 버튼 */}
            <div>
              <button
                type="submit"
                disabled={isLoading}
                className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isLoading ? (
                  <div className="flex items-center">
                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    회원가입 중...
                  </div>
                ) : (
                  '회원가입'
                )}
              </button>
            </div>
          </form>

          {/* 추가 링크들 */}
          <div className="mt-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-gray-500">또는</span>
              </div>
            </div>

            <div className="mt-6 text-center">
              <p className="text-sm text-gray-600">
                이미 계정이 있으신가요?{' '}
                <Link
                  to="/login"
                  className="font-medium text-blue-600 hover:text-blue-500"
                >
                  로그인
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Signup;
