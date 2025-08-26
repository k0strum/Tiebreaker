import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext.jsx';
import { Link } from 'react-router-dom';
import ProfileImage from '../components/ProfileImage';
import axios from '../utils/axios';

const Home = () => {
  const { isLoggedIn, nickname, email, role, profileImg, memberId, isAdmin, loginType, logout } = useAuth();
  const [teamRanks, setTeamRanks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // 팀 순위 데이터 가져오기
  const fetchTeamRanks = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await axios.get('/info/current/teamRank');
      console.log('API 응답:', response.data); // 디버깅용

      // 응답이 배열인지 확인하고 안전하게 설정
      if (Array.isArray(response.data)) {
        setTeamRanks(response.data);
      } else if (response.data && Array.isArray(response.data.data)) {
        setTeamRanks(response.data.data);
      } else {
        console.warn('예상하지 못한 응답 형태:', response.data);
        setTeamRanks([]);
      }
    } catch (err) {
      console.error('팀 순위 데이터 가져오기 실패:', err);
      setError('팀 순위 데이터를 불러오는데 실패했습니다.');
      setTeamRanks([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTeamRanks();
  }, []);

  // 승률 계산 (소수점 3자리까지)
  const formatWinRate = (winRate) => {
    return winRate.toFixed(3);
  };

  // 연속 기록 포맷팅 (String 값으로 받아오므로 그대로 사용)
  const formatStreak = (streak) => {
    if (!streak) return '-';
    return streak; // 이미 '2승', '1패' 등의 형태로 받아오므로 그대로 반환
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 메인 컨텐츠 */}
      <main className="container mx-auto px-4 py-8">
        <div className="max-w-6xl mx-auto">
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
                  <ProfileImage src={profileImg} alt="프로필" size="xl" />
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
                    className="bg-gradient-to-r from-red-500 to-red-600 hover:from-red-600 hover:to-red-700 text-white px-6 py-2 rounded-lg transition-all duration-200 transform hover:scale-[1.02] font-semibold shadow-sm"
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
                    className="bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white px-6 py-2 rounded-lg transition-all duration-200 transform hover:scale-[1.02] font-bold shadow-sm"
                    style={{ color: '#ffffff', textShadow: '0 1px 2px rgba(0,0,0,0.3)' }}
                  >
                    로그인
                  </Link>
                  <Link
                    to="/signup"
                    className="bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white px-6 py-2 rounded-lg transition-all duration-200 transform hover:scale-[1.02] font-bold shadow-sm"
                    style={{ color: '#ffffff', textShadow: '0 1px 2px rgba(0,0,0,0.3)' }}
                  >
                    회원가입
                  </Link>
                </div>
              </div>
            )}
          </div>

          {/* KBO 팀 순위 표 */}
          <div className="bg-white rounded-lg shadow-md p-6 mb-8">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-2xl font-semibold text-gray-800">
                🏟️ KBO 팀 순위
              </h2>
              <button
                onClick={fetchTeamRanks}
                disabled={loading}
                className="bg-blue-500 hover:bg-blue-600 disabled:bg-gray-400 text-white px-4 py-2 rounded-lg transition-colors duration-200 text-sm font-medium"
              >
                {loading ? '새로고침 중...' : '새로고침'}
              </button>
            </div>

            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4">
                {error}
              </div>
            )}

            {loading ? (
              <div className="text-center py-8">
                <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
                <p className="mt-2 text-gray-600">팀 순위를 불러오는 중...</p>
              </div>
            ) : Array.isArray(teamRanks) && teamRanks.length > 0 ? (
              <div className="overflow-x-auto">
                <table className="w-full border-collapse">
                  <thead>
                    <tr className="bg-gray-50 border-b border-gray-200">
                      <th className="text-left py-3 px-4 font-semibold text-gray-700">순위</th>
                      <th className="text-left py-3 px-4 font-semibold text-gray-700">팀</th>
                      <th className="text-center py-3 px-4 font-semibold text-gray-700">경기</th>
                      <th className="text-center py-3 px-4 font-semibold text-gray-700">승</th>
                      <th className="text-center py-3 px-4 font-semibold text-gray-700">무</th>
                      <th className="text-center py-3 px-4 font-semibold text-gray-700">패</th>
                      <th className="text-center py-3 px-4 font-semibold text-gray-700">승률</th>
                      <th className="text-center py-3 px-4 font-semibold text-gray-700">게임차</th>
                      <th className="text-center py-3 px-4 font-semibold text-gray-700">연속</th>
                    </tr>
                  </thead>
                  <tbody>
                    {teamRanks.map((team, index) => (
                      <tr
                        key={team.teamName}
                        className={`border-b border-gray-100 hover:bg-gray-50 transition-colors duration-150 ${index < 5 ? 'bg-blue-50' : ''
                          }`}
                      >
                        <td className="py-3 px-4 font-semibold text-gray-900">
                          {team.rank}
                        </td>
                        <td className="py-3 px-4">
                          <div className="flex items-center space-x-3">
                            {team.teamLogoUrl && (
                              <img
                                src={team.teamLogoUrl}
                                alt={`${team.teamName} 로고`}
                                className="w-8 h-8 object-contain"
                                onError={(e) => {
                                  e.target.style.display = 'none';
                                }}
                              />
                            )}
                            <span className="font-medium text-gray-900">{team.teamName}</span>
                          </div>
                        </td>
                        <td className="py-3 px-4 text-center text-gray-700">{team.plays}</td>
                        <td className="py-3 px-4 text-center text-green-600 font-semibold">{team.wins}</td>
                        <td className="py-3 px-4 text-center text-gray-600">{team.draws}</td>
                        <td className="py-3 px-4 text-center text-red-600 font-semibold">{team.losses}</td>
                        <td className="py-3 px-4 text-center font-semibold text-gray-900">
                          {formatWinRate(team.winRate)}
                        </td>
                        <td className="py-3 px-4 text-center text-gray-700">
                          {team.gameBehind === 0 ? '-' : team.gameBehind}
                        </td>
                        <td className="py-3 px-4 text-center">
                          <span className={`px-2 py-1 rounded-full text-xs font-medium ${team.streak && team.streak.includes('승')
                              ? 'bg-green-100 text-green-800'
                              : team.streak && team.streak.includes('패')
                                ? 'bg-red-100 text-red-800'
                                : team.streak && team.streak.includes('무')
                                  ? 'bg-gray-100 text-gray-800'
                                  : 'bg-gray-100 text-gray-600'
                            }`}>
                            {formatStreak(team.streak)}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="text-center py-8 text-gray-500">
                팀 순위 데이터가 없습니다.
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
                to="/rankings"
                className="text-blue-600 hover:text-blue-700 font-medium"
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
                className="text-blue-600 hover:text-blue-700 font-medium"
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
                className="text-blue-600 hover:text-blue-700 font-medium"
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