import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from '../utils/axios';

function PlayerDetail() {
  const { playerId } = useParams();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('season');
  const [player, setPlayer] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isFavorite, setIsFavorite] = useState(false);

  // 데모 데이터 (실제 API 연동 전까지 사용)
  const demoPlayer = {
    id: playerId || '1',
    name: '김현수',
    team: 'SSG 랜더스',
    position: '좌익수',
    number: '7',
    birthDate: '1988.01.12',
    imageUrl: '/images/default-player.jpg',
    seasonStats: {
      battingAverage: 0.312,
      homeRuns: 25,
      rbi: 89,
      onBasePercentage: 0.389,
      ops: 0.892,
      stolenBases: 8,
      games: 125,
      atBats: 456,
      hits: 142
    },
    monthlyStats: [
      { month: '3월', battingAverage: 0.350, homeRuns: 5, rbi: 18, games: 25 },
      { month: '4월', battingAverage: 0.280, homeRuns: 3, rbi: 15, games: 28 },
      { month: '5월', battingAverage: 0.320, homeRuns: 4, rbi: 22, games: 27 },
      { month: '6월', battingAverage: 0.315, homeRuns: 4, rbi: 20, games: 26 },
      { month: '7월', battingAverage: 0.298, homeRuns: 3, rbi: 12, games: 24 },
      { month: '8월', battingAverage: 0.350, homeRuns: 6, rbi: 22, games: 25 }
    ],
    careerStats: {
      totalGames: 612,
      totalHomeRuns: 156,
      totalRbi: 589,
      totalStolenBases: 45,
      careerBattingAverage: 0.298,
      careerOnBasePercentage: 0.375
    },
    yearlyStats: [
      { year: '2020', battingAverage: 0.285, homeRuns: 18, rbi: 67 },
      { year: '2021', battingAverage: 0.312, homeRuns: 22, rbi: 89 },
      { year: '2022', battingAverage: 0.298, homeRuns: 19, rbi: 78 },
      { year: '2023', battingAverage: 0.305, homeRuns: 21, rbi: 82 },
      { year: '2024', battingAverage: 0.312, homeRuns: 25, rbi: 89 }
    ],
    awards: [
      { year: '2024', title: '7월 타율왕 (3위)', type: 'monthly' },
      { year: '2023', title: '골든글러브 좌익수 부문', type: 'major' },
      { year: '2022', title: '올스타전 선발', type: 'allstar' },
      { year: '2021', title: '타점왕 (2위)', type: 'season' }
    ],
    news: [
      { date: '2024.08.15', title: '김현수, 8월 타율 0.350으로 상승', url: '#' },
      { date: '2024.08.10', title: '김현수, 연속 10경기 안타 기록', url: '#' },
      { date: '2024.08.05', title: '김현수, 시즌 25호 홈런 달성', url: '#' },
      { date: '2024.07.30', title: '김현수, 7월 타율왕 경쟁 치열', url: '#' }
    ]
  };

  useEffect(() => {
    // 실제 API 호출 시뮬레이션
    const fetchPlayerData = async () => {
      setLoading(true);
      try {
        // 실제 API 연동 시: const response = await axios.get(`/players/${playerId}`);
        // 현재는 데모 데이터 사용
        setTimeout(() => {
          setPlayer(demoPlayer);
          setLoading(false);
        }, 500);
      } catch (error) {
        console.error('선수 데이터 로딩 실패:', error);
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
        title: `${player?.name} 선수 기록`,
        text: `${player?.name} 선수의 상세 기록을 확인해보세요!`,
        url: window.location.href
      });
    } else {
      // 폴백: 클립보드에 복사
      navigator.clipboard.writeText(window.location.href);
      alert('링크가 클립보드에 복사되었습니다!');
    }
  };

  const formatValue = (value, type) => {
    switch (type) {
      case 'battingAverage':
      case 'onBasePercentage':
      case 'ops':
        return value.toFixed(3);
      case 'era':
      case 'whip':
        return value.toFixed(2);
      default:
        return value;
    }
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
                <span className="text-white text-sm font-bold">{player.number}</span>
              </div>
            </div>
          </div>

          {/* 프로필 정보 */}
          <div className="lg:col-span-2">
            <h1 className="text-3xl font-bold text-gray-800 mb-2">{player.name}</h1>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <div className="flex items-center space-x-2">
                  <span className="text-gray-600">팀:</span>
                  <span className="font-semibold text-blue-600">{player.team}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <span className="text-gray-600">포지션:</span>
                  <span className="font-semibold">{player.position}</span>
                </div>
              </div>
              <div className="space-y-2">
                <div className="flex items-center space-x-2">
                  <span className="text-gray-600">등번호:</span>
                  <span className="font-semibold">#{player.number}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <span className="text-gray-600">생년월일:</span>
                  <span className="font-semibold">{player.birthDate}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 탭 네비게이션 */}
      <div className="bg-white rounded-lg shadow-md mb-6">
        <div className="flex overflow-x-auto">
          {[
            { id: 'season', name: '시즌성적', icon: '📊' },
            { id: 'monthly', name: '월별추이', icon: '📈' },
            { id: 'career', name: '통산기록', icon: '🏆' },
            { id: 'awards', name: '수상내역', icon: '🎖️' },
            { id: 'news', name: '뉴스', icon: '📰' }
          ].map((tab) => (
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
        {activeTab === 'season' && (
          <div>
            <h2 className="text-2xl font-bold text-gray-800 mb-6">2024 시즌 성적</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <h3 className="text-lg font-semibold text-gray-700 mb-4">타격 성적</h3>
                <div className="space-y-3">
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">타율</span>
                    <span className="font-bold text-blue-600">{formatValue(player.seasonStats.battingAverage, 'battingAverage')}</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">홈런</span>
                    <span className="font-bold text-red-600">{player.seasonStats.homeRuns}개</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">타점</span>
                    <span className="font-bold text-green-600">{player.seasonStats.rbi}점</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">출루율</span>
                    <span className="font-bold text-purple-600">{formatValue(player.seasonStats.onBasePercentage, 'onBasePercentage')}</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">OPS</span>
                    <span className="font-bold text-orange-600">{formatValue(player.seasonStats.ops, 'ops')}</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">도루</span>
                    <span className="font-bold text-teal-600">{player.seasonStats.stolenBases}개</span>
                  </div>
                </div>
              </div>
              <div>
                <h3 className="text-lg font-semibold text-gray-700 mb-4">기타 기록</h3>
                <div className="space-y-3">
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">경기수</span>
                    <span className="font-bold">{player.seasonStats.games}경기</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">타수</span>
                    <span className="font-bold">{player.seasonStats.atBats}타수</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">안타</span>
                    <span className="font-bold">{player.seasonStats.hits}개</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'monthly' && (
          <div>
            <h2 className="text-2xl font-bold text-gray-800 mb-6">월별 성적 변화</h2>
            <div className="mb-6">
              <h3 className="text-lg font-semibold text-gray-700 mb-4">타율 변화</h3>
              <div className="bg-gray-50 p-4 rounded-lg">
                <div className="flex items-end justify-between h-32 mb-4">
                  {player.monthlyStats.map((stat, index) => (
                    <div key={stat.month} className="flex flex-col items-center">
                      <div 
                        className="bg-blue-500 rounded-t w-8 mb-2 transition-all hover:bg-blue-600"
                        style={{ height: `${(stat.battingAverage / 0.4) * 100}%` }}
                      ></div>
                      <span className="text-xs text-gray-600">{stat.battingAverage.toFixed(3)}</span>
                      <span className="text-xs font-medium">{stat.month}</span>
                    </div>
                  ))}
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
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">타율</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">홈런</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">타점</th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">경기수</th>
                    </tr>
                  </thead>
                  <tbody>
                    {player.monthlyStats.map((stat) => (
                      <tr key={stat.month} className="border-b hover:bg-gray-50">
                        <td className="px-4 py-3 font-medium">{stat.month}</td>
                        <td className="px-4 py-3">{stat.battingAverage.toFixed(3)}</td>
                        <td className="px-4 py-3">{stat.homeRuns}개</td>
                        <td className="px-4 py-3">{stat.rbi}점</td>
                        <td className="px-4 py-3">{stat.games}경기</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'career' && (
          <div>
            <h2 className="text-2xl font-bold text-gray-800 mb-6">통산 기록 (2020-2024)</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
              <div>
                <h3 className="text-lg font-semibold text-gray-700 mb-4">통산 성적 요약</h3>
                <div className="space-y-3">
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">총 경기</span>
                    <span className="font-bold">{player.careerStats.totalGames}경기</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">총 홈런</span>
                    <span className="font-bold">{player.careerStats.totalHomeRuns}개</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">총 타점</span>
                    <span className="font-bold">{player.careerStats.totalRbi}점</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">총 도루</span>
                    <span className="font-bold">{player.careerStats.totalStolenBases}개</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">통산 타율</span>
                    <span className="font-bold">{player.careerStats.careerBattingAverage.toFixed(3)}</span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">통산 출루율</span>
                    <span className="font-bold">{player.careerStats.careerOnBasePercentage.toFixed(3)}</span>
                  </div>
                </div>
              </div>
              <div>
                <h3 className="text-lg font-semibold text-gray-700 mb-4">연도별 성적 변화</h3>
                <div className="overflow-x-auto">
                  <table className="min-w-full table-auto">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">연도</th>
                        <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">타율</th>
                        <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">홈런</th>
                        <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">타점</th>
                      </tr>
                    </thead>
                    <tbody>
                      {player.yearlyStats.map((stat) => (
                        <tr key={stat.year} className="border-b hover:bg-gray-50">
                          <td className="px-4 py-3 font-medium">{stat.year}</td>
                          <td className="px-4 py-3">{stat.battingAverage.toFixed(3)}</td>
                          <td className="px-4 py-3">{stat.homeRuns}개</td>
                          <td className="px-4 py-3">{stat.rbi}점</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'awards' && (
          <div>
            <h2 className="text-2xl font-bold text-gray-800 mb-6">수상 내역</h2>
            <div className="space-y-4">
              {player.awards.map((award, index) => (
                <div key={index} className="flex items-center space-x-4 p-4 bg-gray-50 rounded-lg">
                  <div className="text-2xl">
                    {award.type === 'major' ? '🏆' : 
                     award.type === 'allstar' ? '⭐' : 
                     award.type === 'season' ? '🥇' : '🎖️'}
                  </div>
                  <div className="flex-1">
                    <div className="font-semibold text-gray-800">{award.title}</div>
                    <div className="text-sm text-gray-600">{award.year}년</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {activeTab === 'news' && (
          <div>
            <h2 className="text-2xl font-bold text-gray-800 mb-6">관련 뉴스</h2>
            <div className="space-y-4">
              {player.news.map((item, index) => (
                <div key={index} className="border-b pb-4 last:border-b-0">
                  <div className="flex items-start space-x-4">
                    <div className="text-gray-400 text-sm min-w-[80px]">{item.date}</div>
                    <div className="flex-1">
                      <a 
                        href={item.url} 
                        className="text-blue-600 hover:text-blue-800 font-medium hover:underline"
                      >
                        {item.title}
                      </a>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default PlayerDetail;
