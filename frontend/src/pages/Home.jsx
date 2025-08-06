import React from 'react';
import { useAuth } from '../contexts/AuthContext.jsx';
import { Link } from 'react-router-dom';

const Home = () => {
  const { isLoggedIn, nickname, email, role, profileImg, memberId, isAdmin, loginType, logout } = useAuth();

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 메인 컨텐츠 */}
      <main className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          {/* 환영 메시지 */}
          <div className="text-center mb-8">
            <h1 className="text-4xl font-bold text-gray-900 mb-4">
              Tiebreaker에 오신 것을 환영합니다!
            </h1>
          </div>

          {/* 로그인 상태 표시 카드 */}
          <div className="bg-white rounded-lg shadow-md p-6 mb-8">
            <h2 className="text-2xl font-semibold text-gray-800 mb-4">
              🔐 인증 상태 확인
            </h2>
            
            {isLoggedIn ? (
              <div className="space-y-4">
                {/* 사용자 정보 */}
                <div className="flex items-center space-x-4">
                  <img 
                    src={profileImg} 
                    alt="프로필" 
                    className="w-16 h-16 rounded-full border-2 border-gray-200"
                    onError={(e) => {
                      e.target.src = '/images/profile-default.svg';
                    }}
                  />
                  <div>
                    <h3 className="text-xl font-semibold text-gray-800">
                      안녕하세요, {nickname}님! 👋
                    </h3>
                    <p className="text-gray-600">{email}</p>
                  </div>
                </div>

                {/* 상세 정보 */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 bg-gray-50 p-4 rounded-lg">
                  <div>
                    <span className="font-medium text-gray-700">사용자 ID:</span>
                    <span className="ml-2 text-gray-900">{memberId || 'N/A'}</span>
                  </div>
                  <div>
                    <span className="font-medium text-gray-700">역할:</span>
                    <span className="ml-2 text-gray-900">{role}</span>
                  </div>
                  <div>
                    <span className="font-medium text-gray-700">관리자 여부:</span>
                    <span className="ml-2 text-gray-900">
                      {isAdmin ? '✅ 관리자' : '❌ 일반 사용자'}
                    </span>
                  </div>
                  <div>
                    <span className="font-medium text-gray-700">로그인 타입:</span>
                    <span className="ml-2 text-gray-900">
                      {loginType === 'LOCAL' && '일반 로그인'}
                      {loginType === 'GOOGLE' && 'Google 로그인'}
                      {loginType === 'KAKAO' && '카카오 로그인'}
                      {loginType === 'NAVER' && '네이버 로그인'}
                      {!loginType && '알 수 없음'}
                    </span>
                  </div>
                  <div>
                    <span className="font-medium text-gray-700">로그인 상태:</span>
                    <span className="ml-2 text-green-600 font-semibold">✅ 로그인됨</span>
                  </div>
                </div>

                {/* 로그아웃 버튼 */}
                <div className="flex justify-center">
                  <button 
                    onClick={logout}
                    className="bg-red-500 hover:bg-red-600 text-white px-6 py-2 rounded-lg transition-colors"
                  >
                    로그아웃
                  </button>
                </div>
              </div>
            ) : (
              <div className="text-center space-y-4">
                <div className="text-gray-600">
                  <p className="text-lg mb-2">현재 로그인되지 않은 상태입니다.</p>
                  <p>로그인하여 서비스를 이용해보세요!</p>
                </div>
                
                {/* 로그인/회원가입 버튼 */}
                <div className="flex justify-center space-x-4">
                  <Link 
                    to="/login"
                    className="bg-blue-500 hover:bg-blue-600 text-white px-6 py-2 rounded-lg transition-colors"
                  >
                    로그인
                  </Link>
                  <Link 
                    to="/signup"
                    className="bg-green-500 hover:bg-green-600 text-white px-6 py-2 rounded-lg transition-colors"
                  >
                    회원가입
                  </Link>
                </div>
              </div>
            )}
          </div>

          {/* 기능 카드들 */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {/* 플레이어 통계 */}
            <div className="bg-white rounded-lg shadow-md p-6">
              <div className="text-3xl mb-4">📊</div>
              <h3 className="text-xl font-semibold text-gray-800 mb-2">
                플레이어 통계
              </h3>
              <p className="text-gray-600 mb-4">
                선수들의 상세한 통계 정보를 확인해보세요
              </p>
              <Link 
                to="/stats"
                className="text-blue-500 hover:text-blue-600 font-medium"
              >
                통계 보기 →
              </Link>
            </div>

            {/* 예측 게임 */}
            <div className="bg-white rounded-lg shadow-md p-6">
              <div className="text-3xl mb-4">⚽</div>
              <h3 className="text-xl font-semibold text-gray-800 mb-2">
                예측 게임
              </h3>
              <p className="text-gray-600 mb-4">
                경기 결과를 예측하고 포인트를 획득하세요
              </p>
              <Link 
                to="/predictions"
                className="text-blue-500 hover:text-blue-600 font-medium"
              >
                예측하기 →
              </Link>
            </div>

            {/* 채팅 */}
            <div className="bg-white rounded-lg shadow-md p-6">
              <div className="text-3xl mb-4">💬</div>
              <h3 className="text-xl font-semibold text-gray-800 mb-2">
                실시간 채팅
              </h3>
              <p className="text-gray-600 mb-4">
                다른 사용자들과 실시간으로 소통해보세요
              </p>
              <Link 
                to="/chat"
                className="text-blue-500 hover:text-blue-600 font-medium"
              >
                채팅하기 →
              </Link>
            </div>
          </div>

          {/* 개발자 정보 */}
          <div className="mt-12 text-center text-gray-500">
            <p className="text-sm">
              개발 중인 프로젝트입니다. 
              <br />
              로그인/회원가입 기능을 테스트해보세요!
            </p>
          </div>
        </div>
      </main>
    </div>
  );
};

export default Home; 