import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from '../utils/axios';

function PlayerStats() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('batter'); // 'batter' or 'pitcher'
  const [selectedRanking, setSelectedRanking] = useState(null); // 선택된 랭킹 (더보기 클릭 시)
  
  // API 데이터 상태 관리
  const [rankings, setRankings] = useState({});
  const [loading, setLoading] = useState({});
  const [error, setError] = useState({});

  // API 엔드포인트 매핑
  const API_ENDPOINTS = {
    // 타자 랭킹
    'batting-average': '/rankings/batting-average',
    'home-runs': '/rankings/home-runs',
    'rbi': '/rankings/rbi',
    'on-base-percentage': '/rankings/on-base-percentage',
    'ops': '/rankings/ops',
    'stolen-bases': '/rankings/stolen-bases',
    
    // 투수 랭킹
    'wins': '/rankings/wins',
    'saves': '/rankings/saves',
    'holds': '/rankings/holds',
    'strikeouts': '/rankings/strikeouts',
    'era': '/rankings/era',
    'whip': '/rankings/whip'
  };

  // 타자 랭킹 옵션
  const batterRankings = [
    { id: 'batting-average', name: '타율', icon: '⚾', color: 'blue' },
    { id: 'home-runs', name: '홈런', icon: '🏟️', color: 'red' },
    { id: 'rbi', name: '타점', icon: '🎯', color: 'green' },
    { id: 'on-base-percentage', name: '출루율', icon: '🚶', color: 'purple' },
    { id: 'ops', name: 'OPS', icon: '📊', color: 'orange' },
    { id: 'stolen-bases', name: '도루', icon: '🏃', color: 'teal' }
  ];

  // 투수 랭킹 옵션
  const pitcherRankings = [
    { id: 'wins', name: '승수', icon: '🏆', color: 'yellow' },
    { id: 'saves', name: '세이브', icon: '💾', color: 'indigo' },
    { id: 'holds', name: '홀드', icon: '🤝', color: 'pink' },
    { id: 'strikeouts', name: '탈삼진', icon: '🔥', color: 'red' },
    { id: 'era', name: '방어율', icon: '🛡️', color: 'blue' },
    { id: 'whip', name: 'WHIP', icon: '⚡', color: 'green' }
  ];

  // API에서 랭킹 데이터 가져오기
  const fetchRanking = async (rankingId) => {
    try {
      setLoading(prev => ({ ...prev, [rankingId]: true }));
      setError(prev => ({ ...prev, [rankingId]: null }));
      
      const response = await axios.get(API_ENDPOINTS[rankingId]);
      setRankings(prev => ({ 
        ...prev, 
        [rankingId]: response.data 
      }));
    } catch (err) {
      console.error(`${rankingId} 랭킹 조회 실패:`, err);
      setError(prev => ({ 
        ...prev, 
        [rankingId]: '랭킹 데이터를 불러오는데 실패했습니다.' 
      }));
    } finally {
      setLoading(prev => ({ ...prev, [rankingId]: false }));
    }
  };

  // API 응답을 프론트엔드 형식으로 변환
  const transformApiData = (apiData, rankingId) => {
    if (!apiData || !Array.isArray(apiData)) {
      return [];
    }
    
    return apiData.map(player => ({
      rank: player.rank,
      name: player.playerName,
      team: player.teamName,
      value: formatValue(player, rankingId),
      image: player.imageUrl || '/images/default-player.jpg'
    }));
  };

  // 랭킹별 값 포맷팅
  const formatValue = (player, rankingId) => {
    const valueMap = {
      'batting-average': player.battingAverage?.toFixed(3) || '0.000',
      'home-runs': `${player.homeRuns || 0}개`,
      'rbi': `${player.runsBattedIn || 0}점`,
      'on-base-percentage': player.onBasePercentage?.toFixed(3) || '0.000',
      'ops': player.ops?.toFixed(3) || '0.000',
      'stolen-bases': `${player.stolenBases || 0}개`,
      'wins': `${player.wins || 0}승`,
      'saves': `${player.saves || 0}개`,
      'holds': `${player.holds || 0}개`,
      'strikeouts': `${player.strikeoutsPitched || 0}개`,
      'era': player.earnedRunAverage?.toFixed(2) || '0.00',
      'whip': player.whip?.toFixed(2) || '0.00'
    };
    return valueMap[rankingId] || '0';
  };

  // 컴포넌트 마운트 시 모든 랭킹 데이터 로딩
  useEffect(() => {
    const loadAllRankings = async () => {
      const allRankingIds = [
        ...batterRankings.map(r => r.id),
        ...pitcherRankings.map(r => r.id)
      ];
      
      // 병렬로 모든 랭킹 데이터 로딩
      await Promise.all(
        allRankingIds.map(rankingId => fetchRanking(rankingId))
      );
    };
    
    loadAllRankings();
  }, []);

  const handleTabChange = (tab) => {
    setActiveTab(tab);
    setSelectedRanking(null); // 탭 변경 시 상세 리스트 닫기
  };

  const handleMoreClick = (rankingId) => {
    setSelectedRanking(selectedRanking === rankingId ? null : rankingId);
  };

  const handlePlayerClick = (playerName) => {
    // 실제 구현 시에는 playerId를 사용해야 함
    navigate(`/player-detail/${encodeURIComponent(playerName)}`);
  };

  const currentRankings = activeTab === 'batter' ? batterRankings : pitcherRankings;

  // 로딩 카드 컴포넌트
  const LoadingCard = ({ ranking }) => (
    <div className="bg-white rounded-lg shadow-md p-4">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center space-x-2">
          <span className="text-2xl">{ranking.icon}</span>
          <h3 className="text-lg font-semibold text-gray-800">{ranking.name}</h3>
        </div>
      </div>
      <div className="space-y-3">
        <div className="animate-pulse">
          <div className="h-16 bg-gray-200 rounded-lg mb-3"></div>
          <div className="space-y-2">
            <div className="h-8 bg-gray-200 rounded"></div>
            <div className="h-8 bg-gray-200 rounded"></div>
            <div className="h-8 bg-gray-200 rounded"></div>
          </div>
        </div>
      </div>
    </div>
  );

  // 에러 카드 컴포넌트
  const ErrorCard = ({ ranking, error }) => (
    <div className="bg-white rounded-lg shadow-md p-4">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center space-x-2">
          <span className="text-2xl">{ranking.icon}</span>
          <h3 className="text-lg font-semibold text-gray-800">{ranking.name}</h3>
        </div>
        <button 
          onClick={() => fetchRanking(ranking.id)}
          className="text-sm text-blue-600 hover:text-blue-800 font-medium"
        >
          재시도
        </button>
      </div>
      <div className="text-center py-8">
        <div className="text-red-500 mb-2">⚠️</div>
        <p className="text-sm text-gray-600">{error}</p>
      </div>
    </div>
  );

  const RankingCard = ({ ranking, data }) => {
    const rankingId = ranking.id;
    const isLoading = loading[rankingId];
    const hasError = error[rankingId];
    const rankingData = rankings[rankingId];
    
    if (isLoading) {
      return <LoadingCard ranking={ranking} />;
    }
    
    if (hasError) {
      return <ErrorCard ranking={ranking} error={hasError} />;
    }
    
    const transformedData = transformApiData(rankingData, rankingId);
    const topPlayer = transformedData[0];
    const otherPlayers = transformedData.slice(1, 4);
    const isSelected = selectedRanking === ranking.id;

    // 데이터가 없을 경우
    if (!topPlayer) {
      return (
        <div className="bg-white rounded-lg shadow-md p-4">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center space-x-2">
              <span className="text-2xl">{ranking.icon}</span>
              <h3 className="text-lg font-semibold text-gray-800">{ranking.name}</h3>
            </div>
          </div>
          <div className="text-center py-8 text-gray-500">
            데이터가 없습니다.
          </div>
        </div>
      );
    }

    return (
      <div className={`bg-white rounded-lg shadow-md p-4 transition-all ${
        isSelected ? 'ring-2 ring-blue-500 shadow-lg' : 'hover:shadow-lg'
      }`}>
        {/* 카드 헤더 */}
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-2">
            <span className="text-2xl">{ranking.icon}</span>
            <h3 className="text-lg font-semibold text-gray-800">{ranking.name}</h3>
          </div>
          <button 
            onClick={() => handleMoreClick(ranking.id)}
            className={`text-sm font-medium transition-colors ${
              isSelected 
                ? 'text-blue-800 bg-blue-100 px-3 py-1 rounded-full' 
                : 'text-blue-600 hover:text-blue-800'
            }`}
          >
            {isSelected ? '접기' : '더보기'}
          </button>
        </div>

        {/* 1위 선수 (강조) */}
        <div className="mb-4 p-3 bg-gray-50 rounded-lg">
          <div className="flex items-center space-x-3">
            <div className="relative">
              <div className="w-12 h-12 bg-gray-300 rounded-full flex items-center justify-center">
                <span className="text-lg">👤</span>
              </div>
              <div className="absolute -top-1 -right-1 w-6 h-6 bg-yellow-400 rounded-full flex items-center justify-center">
                <span className="text-xs">🥇</span>
              </div>
            </div>
            <div className="flex-1">
              <button 
                onClick={() => handlePlayerClick(topPlayer.name)}
                className="text-left hover:underline cursor-pointer"
              >
                <div className="font-semibold text-gray-900">{topPlayer.name}</div>
                <div className="text-sm text-gray-600">{topPlayer.team}</div>
              </button>
            </div>
            <div className="text-right">
              <div className={`text-xl font-bold text-${ranking.color}-600`}>
                {topPlayer.value}
              </div>
            </div>
          </div>
        </div>

        {/* 2-4위 선수들 */}
        <div className="space-y-2">
          {otherPlayers.map((player, index) => (
            <div key={player.rank} className="flex items-center justify-between py-1">
              <div className="flex items-center space-x-2">
                <div className="w-6 h-6 bg-gray-200 rounded-full flex items-center justify-center">
                  <span className="text-xs">👤</span>
                </div>
                <button 
                  onClick={() => handlePlayerClick(player.name)}
                  className="text-left hover:underline cursor-pointer"
                >
                  <div className="text-sm font-medium text-gray-800">{player.name}</div>
                  <div className="text-xs text-gray-500">{player.team}</div>
                </button>
              </div>
              <div className="text-sm font-semibold text-gray-700">{player.value}</div>
            </div>
          ))}
        </div>
      </div>
    );
  };

  // 상세 리스트 컴포넌트
  const DetailedRankingList = ({ ranking, data }) => {
    const rankingId = ranking.id;
    const rankingData = rankings[rankingId];
    const transformedData = transformApiData(rankingData, rankingId);
    const allPlayers = transformedData.slice(0, 10); // 상위 10명

    return (
      <div className="bg-white rounded-lg shadow-md p-6 mb-6 animate-fadeIn">
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center space-x-3">
            <span className="text-3xl">{ranking.icon}</span>
            <h2 className="text-2xl font-bold text-gray-800">{ranking.name} 순위</h2>
          </div>
          <button 
            onClick={() => setSelectedRanking(null)}
            className="text-gray-500 hover:text-gray-700 text-lg"
          >
            ✕
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="min-w-full table-auto">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">순위</th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">선수명</th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">팀</th>
                <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">{ranking.name}</th>
              </tr>
            </thead>
            <tbody>
              {allPlayers.map((player, index) => (
                <tr key={player.rank} className={`border-b hover:bg-gray-50 ${
                  index < 3 ? 'bg-yellow-50' : ''
                }`}>
                  <td className="px-4 py-3">
                    <div className="flex items-center space-x-2">
                      {index < 3 && (
                        <span className="text-lg">
                          {index === 0 ? '🥇' : index === 1 ? '🥈' : '🥉'}
                        </span>
                      )}
                      <span className="font-semibold text-gray-700">{player.rank}</span>
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center space-x-3">
                      <div className="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center">
                        <span className="text-sm">👤</span>
                      </div>
                      <span className="font-medium text-gray-900">{player.name}</span>
                    </div>
                  </td>
                  <td className="px-4 py-3 text-gray-600">{player.team}</td>
                  <td className="px-4 py-3">
                    <span className={`font-bold text-${ranking.color}-600`}>
                      {player.value}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="mt-4 text-sm text-gray-500 text-center">
          상위 10명 표시 • 실시간 업데이트
        </div>
      </div>
    );
  };

  return (
    <div className="container mx-auto p-8">
      <h1 className="text-3xl font-bold text-blue-600 mb-6">📊 선수 기록실</h1>
      
      {/* 탭 네비게이션 */}
      <div className="bg-white rounded-lg shadow-md p-6 mb-6">
        <div className="flex space-x-1 mb-6">
          <button
            onClick={() => handleTabChange('batter')}
            className={`flex-1 py-3 px-4 rounded-lg font-semibold transition-colors ${
              activeTab === 'batter'
                ? 'bg-blue-500 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            🏏 타자 랭킹
          </button>
          <button
            onClick={() => handleTabChange('pitcher')}
            className={`flex-1 py-3 px-4 rounded-lg font-semibold transition-colors ${
              activeTab === 'pitcher'
                ? 'bg-blue-500 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            ⚾ 투수 랭킹
          </button>
        </div>

        {/* 상세 리스트 (더보기 클릭 시) */}
        {selectedRanking && (
          <DetailedRankingList
            ranking={currentRankings.find(r => r.id === selectedRanking)}
            data={rankings[selectedRanking]}
          />
        )}

        {/* 바둑판 형식 랭킹 카드들 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {currentRankings.map((ranking) => (
            <RankingCard
              key={ranking.id}
              ranking={ranking}
              data={rankings[ranking.id]}
            />
          ))}
        </div>
      </div>

      {/* 정보 패널 */}
      <div className="mt-6 bg-blue-50 rounded-lg p-4">
        <h3 className="font-semibold text-blue-800 mb-2">ℹ️ 랭킹 기준 안내</h3>
        <div className="text-sm text-blue-700 space-y-1">
          <p>• <strong>타율/출루율/OPS</strong>: 팀별 규정타석(경기수 × 3.1) 이상인 선수만 집계</p>
          <p>• <strong>방어율/WHIP</strong>: 팀별 규정이닝(경기수 × 1.0) 이상인 선수만 집계</p>
          <p>• <strong>기타 지표</strong>: 0 이상인 모든 선수 집계</p>
        </div>
      </div>
    </div>
  );
}

export default PlayerStats; 