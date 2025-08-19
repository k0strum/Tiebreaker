import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from '../utils/axios';

function PlayerDetail() {
  const { playerId } = useParams();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('profile');
  const [player, setPlayer] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isFavorite, setIsFavorite] = useState(false);
  

  useEffect(() => {
    const fetchPlayerData = async () => {
      setLoading(true);
      try {
        // 백엔드 매핑: GET /api/info/players/{playerId} (baseURL에 /api 포함됨)
        const { data } = await axios.get(`/info/players/${playerId}`);
        setPlayer(data);
        setLoading(false);
      } catch (error) {
        console.error('선수 데이터 로딩 실패:', error);
        setPlayer(null);
        setLoading(false);
      }
    };

    fetchPlayerData();
  }, [playerId]);

  const handleBack = () => {
    navigate('/stats');
  };

  const handleFavorite = () => {
    setIsFavorite(!isFavorite);
  };

  const handleShare = () => {
    if (navigator.share) {
      navigator.share({
        title: `${player?.playerName} 선수 기록`,
        text: `${player?.playerName} 선수의 상세 기록을 확인해보세요!`,
        url: window.location.href
      });
    } else {
      navigator.clipboard.writeText(window.location.href);
      alert('링크가 클립보드에 복사되었습니다!');
    }
  };

  const formatValue = (value, type) => {
    if (value === null || value === undefined) return '-';
    
    switch (type) {
      case 'battingAverage':
      case 'onBasePercentage':
      case 'sluggingPercentage':
      case 'ops':
      case 'stolenBasePercentage':
      case 'battingAverageWithRunnersInScoringPosition':
      case 'pinchHitBattingAverage':
      case 'earnedRunAverage':
      case 'whip':
      case 'battingAverageAgainst':
      case 'winningPercentage':
        return value.toFixed(3);
      default:
        return value;
    }
  };

  const formatInnings = (integer, fraction) => {
    if (integer === null || integer === undefined) return '-';
    if (fraction === null || fraction === undefined) return `${integer}.0`;
    return `${integer}.${fraction}`;
  };

  const getAvailableTabs = () => {
    const tabs = [];
    
    // 기본 정보는 항상 표시
    tabs.push({ id: 'profile', name: '기본정보', icon: '👤' });
    
    // 타자 기록이 있으면 타자 탭들 추가
    if (player?.batterStats) {
      tabs.push({ id: 'batter-season', name: '타자시즌', icon: '⚾' });
      if (player?.batterMonthlyStats?.length > 0) {
        tabs.push({ id: 'batter-monthly', name: '타자월별', icon: '📈' });
      }
    }
    
    // 투수 기록이 있으면 투수 탭들 추가
    if (player?.pitcherStats) {
      tabs.push({ id: 'pitcher-season', name: '투수시즌', icon: '🎯' });
      if (player?.pitcherMonthlyStats?.length > 0) {
        tabs.push({ id: 'pitcher-monthly', name: '투수월별', icon: '📊' });
      }
    }
    
    // 기록이 없으면 기본 탭만
    if (tabs.length === 1) {
      tabs.push({ id: 'no-stats', name: '기록없음', icon: '⚠️' });
    }
    
    return tabs;
  };

  if (loading) {
    return (
      <div className="container mx-auto p-8">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 rounded mb-6"></div>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="h-64 bg-gray-200 rounded"></div>
            <div className="lg:col-span-2 space-y-4">
              <div className="h-12 bg-gray-200 rounded"></div>
              <div className="h-32 bg-gray-200 rounded"></div>
              <div className="h-32 bg-gray-200 rounded"></div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (!player) {
    return (
      <div className="container mx-auto p-8 text-center">
        <div className="text-red-500 text-xl mb-4">⚠️</div>
        <h2 className="text-xl font-semibold text-gray-800 mb-2">선수를 찾을 수 없습니다</h2>
        <p className="text-gray-600 mb-4">요청하신 선수의 정보가 존재하지 않습니다.</p>
        <button
          onClick={handleBack}
          className="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600"
        >
          목록으로 돌아가기
        </button>
      </div>
    );
  }

  const availableTabs = getAvailableTabs();
  const hasAnyStats = player.batterStats || player.pitcherStats;

  return (
    <div className="container mx-auto p-8">
      {/* 헤더 */}
      <div className="flex items-center justify-between mb-6">
        <button
          onClick={handleBack}
          className="flex items-center space-x-2 text-blue-600 hover:text-blue-800 font-medium"
        >
          <span>←</span>
          <span>뒤로가기</span>
        </button>
        <div className="flex items-center space-x-3">
          <button
            onClick={handleFavorite}
            className={`text-2xl transition-colors ${
              isFavorite ? 'text-yellow-500' : 'text-gray-400 hover:text-yellow-500'
            }`}
          >
            {isFavorite ? '⭐' : '☆'}
          </button>
          <button
            onClick={handleShare}
            className="text-gray-600 hover:text-gray-800 text-lg"
          >
            📤
          </button>
        </div>
      </div>

      {/* 프로필 섹션 */}
      <div className="bg-white rounded-lg shadow-md p-6 mb-6">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* 프로필 이미지 */}
          <div className="flex justify-center lg:justify-start">
            <div className="relative">
              <div className="w-32 h-32 bg-gray-300 rounded-full flex items-center justify-center">
                <span className="text-4xl">👤</span>
              </div>
              <div className="absolute -bottom-2 -right-2 w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
                <span className="text-white text-sm font-bold">{player.backNumber}</span>
              </div>
            </div>
          </div>

          {/* 프로필 정보 */}
          <div className="lg:col-span-2">
            <h1 className="text-3xl font-bold text-gray-800 mb-2">{player.playerName}</h1>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <div className="flex items-center space-x-2">
                  <span className="text-gray-600">팀:</span>
                  <span className="font-semibold text-blue-600">{player.teamName}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <span className="text-gray-600">포지션:</span>
                  <span className="font-semibold">{player.position}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <span className="text-gray-600">등번호:</span>
                  <span className="font-semibold">#{player.backNumber}</span>
                </div>
              </div>
              <div className="space-y-2">
                <div className="flex items-center space-x-2">
                  <span className="text-gray-600">생년월일:</span>
                  <span className="font-semibold">{player.birthday}</span>
                </div>
                {player.heightWeight && (
                  <div className="flex items-center space-x-2">
                    <span className="text-gray-600">신체:</span>
                    <span className="font-semibold">{player.heightWeight}</span>
                  </div>
                )}
                {player.draftRank && (
                  <div className="flex items-center space-x-2">
                    <span className="text-gray-600">지명:</span>
                    <span className="font-semibold">{player.draftRank}</span>
                  </div>
                )}
              </div>
            </div>
            {player.career && (
              <div className="mt-4 p-3 bg-gray-50 rounded-lg">
                <span className="text-gray-600">경력: </span>
                <span className="font-medium">{player.career}</span>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 탭 네비게이션 */}
      <div className="bg-white rounded-lg shadow-md mb-6">
        <div className="flex overflow-x-auto">
          {availableTabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center space-x-2 px-6 py-4 font-semibold transition-colors whitespace-nowrap ${
                activeTab === tab.id
                  ? 'bg-blue-500 text-white border-b-2 border-blue-500'
                  : 'bg-white text-gray-700 hover:bg-gray-50 border-b-2 border-transparent'
              }`}
            >
              <span>{tab.icon}</span>
              <span>{tab.name}</span>
            </button>
          ))}
        </div>
      </div>

      {/* 탭 내용 */}
      <div className="bg-white rounded-lg shadow-md p-6">
        {activeTab === 'profile' && (
          <div>
            <h2 className="text-2xl font-bold text-gray-800 mb-6">기본 정보</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <h3 className="text-lg font-semibold text-gray-700 mb-4">개인 정보</h3>
                <div className="space-y-3">
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">이름</span>
                    <span className="font-bold">{player.playerName}</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">팀</span>
                    <span className="font-bold text-blue-600">{player.teamName}</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">포지션</span>
                    <span className="font-bold">{player.position}</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">등번호</span>
                    <span className="font-bold">#{player.backNumber}</span>
                  </div>
                </div>
              </div>
              <div>
                <h3 className="text-lg font-semibold text-gray-700 mb-4">상세 정보</h3>
                <div className="space-y-3">
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">생년월일</span>
                    <span className="font-bold">{player.birthday}</span>
                  </div>
                  {player.heightWeight && (
                    <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                      <span className="text-gray-600">신체 조건</span>
                      <span className="font-bold">{player.heightWeight}</span>
                    </div>
                  )}
                  {player.draftRank && (
                    <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                      <span className="text-gray-600">지명 순위</span>
                      <span className="font-bold">{player.draftRank}</span>
                    </div>
                  )}
                  {player.career && (
                    <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                      <span className="text-gray-600">경력</span>
                      <span className="font-bold">{player.career}</span>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'batter-season' && player.batterStats && (
          <div>
            <h2 className="text-2xl font-bold text-gray-800 mb-6">{player.batterStats.year} 시즌 타격 성적</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <h3 className="text-lg font-semibold text-gray-700 mb-4">주요 타격 성적</h3>
                <div className="space-y-3">
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">타율</span>
                    <span className="font-bold text-blue-600">{formatValue(player.batterStats.battingAverage, 'battingAverage')}</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">홈런</span>
                    <span className="font-bold text-red-600">{player.batterStats.homeRuns}개</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">타점</span>
                    <span className="font-bold text-green-600">{player.batterStats.runsBattedIn}점</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">출루율</span>
                    <span className="font-bold text-purple-600">{formatValue(player.batterStats.onBasePercentage, 'onBasePercentage')}</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">장타율</span>
                    <span className="font-bold text-orange-600">{formatValue(player.batterStats.sluggingPercentage, 'sluggingPercentage')}</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">OPS</span>
                    <span className="font-bold text-indigo-600">{formatValue(player.batterStats.ops, 'ops')}</span>
                  </div>
                </div>
              </div>
              <div>
                <h3 className="text-lg font-semibold text-gray-700 mb-4">기타 기록</h3>
                <div className="space-y-3">
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">경기수</span>
                    <span className="font-bold">{player.batterStats.games}경기</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">타석</span>
                    <span className="font-bold">{player.batterStats.plateAppearances}타석</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">타수</span>
                    <span className="font-bold">{player.batterStats.atBats}타수</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">안타</span>
                    <span className="font-bold">{player.batterStats.hits}개</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">도루</span>
                    <span className="font-bold text-teal-600">{player.batterStats.stolenBases}개</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">득점</span>
                    <span className="font-bold">{player.batterStats.runs}점</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'batter-monthly' && player.batterMonthlyStats && player.batterMonthlyStats.length > 0 && (
          <div>
            <h2 className="text-2xl font-bold text-gray-800 mb-6">월별 타격 성적 변화</h2>
            <div className="mb-6">
              <h3 className="text-lg font-semibold text-gray-700 mb-4">타율 변화</h3>
              <div className="bg-gray-50 p-4 rounded-lg">
                <div className="flex items-end justify-between h-32 mb-4">
                  {player.batterMonthlyStats.map((stat) => {
                    const battingAverage = stat.atBats > 0 ? stat.hits / stat.atBats : 0;
                    return (
                      <div key={`${stat.year}-${stat.month}`} className="flex flex-col items-center">
                        <div 
                          className="bg-blue-500 rounded-t w-8 mb-2 transition-all hover:bg-blue-600"
                          style={{ height: `${(battingAverage / 0.4) * 100}%` }}
                        ></div>
                        <span className="text-xs text-gray-600">{battingAverage.toFixed(3)}</span>
                        <span className="text-xs font-medium">{stat.month}월</span>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
            <div>
              <h3 className="text-lg font-semibold text-gray-700 mb-4">월별 상세 기록</h3>
              <div className="overflow-x-auto">
                <table className="min-w-full table-auto">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">월</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">경기</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">타율</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">홈런</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">타점</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">안타</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">볼넷</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">삼진</th>
                    </tr>
                  </thead>
                  <tbody>
                    {player.batterMonthlyStats.map((stat) => {
                      const battingAverage = stat.atBats > 0 ? stat.hits / stat.atBats : 0;
                      return (
                        <tr key={`${stat.year}-${stat.month}`} className="border-b hover:bg-gray-50">
                          <td className="px-4 py-3 font-medium">{stat.month}월</td>
                          <td className="px-4 py-3">{stat.games}경기</td>
                          <td className="px-4 py-3">{battingAverage.toFixed(3)}</td>
                          <td className="px-4 py-3">{stat.homeRuns}개</td>
                          <td className="px-4 py-3">{stat.runsBattedIn}점</td>
                          <td className="px-4 py-3">{stat.hits}개</td>
                          <td className="px-4 py-3">{stat.walks}개</td>
                          <td className="px-4 py-3">{stat.strikeouts}개</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'pitcher-season' && player.pitcherStats && (
          <div>
            <h2 className="text-2xl font-bold text-gray-800 mb-6">{player.pitcherStats.year} 시즌 투구 성적</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <h3 className="text-lg font-semibold text-gray-700 mb-4">주요 투구 성적</h3>
                <div className="space-y-3">
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">평균자책점</span>
                    <span className="font-bold text-blue-600">{formatValue(player.pitcherStats.earnedRunAverage, 'earnedRunAverage')}</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">승수</span>
                    <span className="font-bold text-green-600">{player.pitcherStats.wins}승</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">패수</span>
                    <span className="font-bold text-red-600">{player.pitcherStats.losses}패</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">세이브</span>
                    <span className="font-bold text-purple-600">{player.pitcherStats.saves}개</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">홀드</span>
                    <span className="font-bold text-orange-600">{player.pitcherStats.holds}개</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">WHIP</span>
                    <span className="font-bold text-indigo-600">{formatValue(player.pitcherStats.whip, 'whip')}</span>
                  </div>
                </div>
              </div>
              <div>
                <h3 className="text-lg font-semibold text-gray-700 mb-4">기타 기록</h3>
                <div className="space-y-3">
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">경기수</span>
                    <span className="font-bold">{player.pitcherStats.games}경기</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">이닝</span>
                    <span className="font-bold">{formatInnings(player.pitcherStats.inningsPitchedInteger, player.pitcherStats.inningsPitchedFraction)}이닝</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">삼진</span>
                    <span className="font-bold">{player.pitcherStats.strikeouts}개</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">완투</span>
                    <span className="font-bold">{player.pitcherStats.completeGames}개</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">완봉</span>
                    <span className="font-bold">{player.pitcherStats.shutouts}개</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">퀄리티스타트</span>
                    <span className="font-bold">{player.pitcherStats.qualityStarts}개</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'pitcher-monthly' && player.pitcherMonthlyStats && player.pitcherMonthlyStats.length > 0 && (
          <div>
            <h2 className="text-2xl font-bold text-gray-800 mb-6">월별 투구 성적 변화</h2>
            <div className="mb-6">
              <h3 className="text-lg font-semibold text-gray-700 mb-4">평균자책점 변화</h3>
              <div className="bg-gray-50 p-4 rounded-lg">
                <div className="flex items-end justify-between h-32 mb-4">
                  {player.pitcherMonthlyStats.map((stat) => {
                    const era = stat.inningsPitchedInteger > 0 ? (stat.earnedRuns * 9) / (stat.inningsPitchedInteger + stat.inningsPitchedFraction / 3) : 0;
                    return (
                      <div key={`${stat.year}-${stat.month}`} className="flex flex-col items-center">
                        <div 
                          className="bg-red-500 rounded-t w-8 mb-2 transition-all hover:bg-red-600"
                          style={{ height: `${Math.min((era / 5) * 100, 100)}%` }}
                        ></div>
                        <span className="text-xs text-gray-600">{era.toFixed(2)}</span>
                        <span className="text-xs font-medium">{stat.month}월</span>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
            <div>
              <h3 className="text-lg font-semibold text-gray-700 mb-4">월별 상세 기록</h3>
              <div className="overflow-x-auto">
                <table className="min-w-full table-auto">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">월</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">경기</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">이닝</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">평균자책점</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">승수</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">패수</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">세이브</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">홀드</th>
                    </tr>
                  </thead>
                  <tbody>
                    {player.pitcherMonthlyStats.map((stat) => {
                      const era = stat.inningsPitchedInteger > 0 ? (stat.earnedRuns * 9) / (stat.inningsPitchedInteger + stat.inningsPitchedFraction / 3) : 0;
                      return (
                        <tr key={`${stat.year}-${stat.month}`} className="border-b hover:bg-gray-50">
                          <td className="px-4 py-3 font-medium">{stat.month}월</td>
                          <td className="px-4 py-3">{stat.games}경기</td>
                          <td className="px-4 py-3">{formatInnings(stat.inningsPitchedInteger, stat.inningsPitchedFraction)}이닝</td>
                          <td className="px-4 py-3">{era.toFixed(2)}</td>
                          <td className="px-4 py-3">{stat.wins}승</td>
                          <td className="px-4 py-3">{stat.losses}패</td>
                          <td className="px-4 py-3">{stat.saves}개</td>
                          <td className="px-4 py-3">{stat.holds}개</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'no-stats' && !hasAnyStats && (
          <div className="text-center py-12">
            <div className="text-gray-400 text-6xl mb-4">📊</div>
            <h2 className="text-2xl font-bold text-gray-800 mb-4">경기 기록이 없습니다</h2>
            <p className="text-gray-600 mb-6">
              {player.playerName} 선수의 {new Date().getFullYear()} 시즌 경기 기록이 없습니다.
            </p>
            <div className="bg-gray-50 rounded-lg p-6 max-w-md mx-auto">
              <h3 className="font-semibold text-gray-700 mb-2">가능한 이유:</h3>
              <ul className="text-sm text-gray-600 space-y-1 text-left">
                <li>• 아직 경기에 출전하지 않음</li>
                <li>• 부상으로 인한 결장</li>
                <li>• 2군에서 활동 중</li>
                <li>• 시즌 중 입단</li>
              </ul>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default PlayerDetail;
