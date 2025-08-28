import React, { useState, useEffect } from 'react';
import axios from 'axios';

const LiveGames = () => {
  const [liveGames, setLiveGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchLiveGames();
    // 5초마다 실시간 경기 목록 갱신
    const interval = setInterval(fetchLiveGames, 5000);
    return () => clearInterval(interval);
  }, []);

  const fetchLiveGames = async () => {
    try {
      const response = await axios.get('/api/live-games/live');
      setLiveGames(response.data);
      setLoading(false);
    } catch (err) {
      console.error('실시간 경기 목록 조회 실패:', err);
      setError('실시간 경기 목록을 불러오는데 실패했습니다.');
      setLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'READY':
        return <span className="px-2 py-1 text-xs font-semibold bg-yellow-100 text-yellow-800 rounded-full">예정</span>;
      case 'LIVE':
        return <span className="px-2 py-1 text-xs font-semibold bg-red-100 text-red-800 rounded-full animate-pulse">LIVE</span>;
      case 'FINISHED':
        return <span className="px-2 py-1 text-xs font-semibold bg-gray-100 text-gray-800 rounded-full">종료</span>;
      default:
        return <span className="px-2 py-1 text-xs font-semibold bg-gray-100 text-gray-800 rounded-full">{status}</span>;
    }
  };

  const formatInning = (inning, half) => {
    if (inning === 0) return '경기전';
    const halfText = half === 'T' ? '초' : '말';
    return `${inning}회${halfText}`;
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-4 text-gray-600">실시간 경기 정보를 불러오는 중...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
              <p>{error}</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">실시간 경기</h1>
          <p className="mt-2 text-gray-600">현재 진행 중인 KBO 경기 현황</p>
        </div>

        {liveGames.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-gray-500">
              <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
              <h3 className="mt-2 text-sm font-medium text-gray-900">진행 중인 경기가 없습니다</h3>
              <p className="mt-1 text-sm text-gray-500">현재 진행 중인 KBO 경기가 없습니다.</p>
            </div>
          </div>
        ) : (
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {liveGames.map((game) => (
              <div
                key={game.gameId}
                className={`bg-white rounded-lg shadow-md p-6 border-l-4 ${game.status === 'LIVE' ? 'border-red-500' : 'border-blue-500'
                  }`}
              >
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <div className="flex items-center justify-between mb-2">
                      <h3 className="text-lg font-semibold text-gray-900">
                        {game.awayTeam} vs {game.homeTeam}
                      </h3>
                      {getStatusBadge(game.status)}
                    </div>
                    <p className="text-sm text-gray-600">{formatInning(game.inning, game.half)}</p>
                  </div>
                </div>

                <div className="space-y-3">
                  <div className="flex justify-between items-center">
                    <span className="text-sm font-medium text-gray-700">{game.awayTeam}</span>
                    <span className="text-lg font-bold text-gray-900">{game.awayScore}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-sm font-medium text-gray-700">{game.homeTeam}</span>
                    <span className="text-lg font-bold text-gray-900">{game.homeScore}</span>
                  </div>
                </div>

                {game.status === 'LIVE' && (
                  <div className="mt-4 pt-4 border-t border-gray-200">
                    <div className="text-xs text-gray-600 space-y-1">
                      <p><span className="font-medium">타자:</span> {game.currentBatter}</p>
                      <p><span className="font-medium">투수:</span> {game.currentPitcher}</p>
                      <p><span className="font-medium">아웃:</span> {game.outs}개</p>
                      <p><span className="font-medium">볼-스트라이크:</span> {game.balls}-{game.strikes}</p>
                    </div>
                  </div>
                )}

                <div className="mt-4">
                  <a
                    href={`/live-game/${game.gameId}`}
                    className="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-md text-center block transition duration-200"
                  >
                    실시간 중계 보기
                  </a>
                </div>
              </div>
            ))}
          </div>
        )}

        <div className="mt-8 text-center">
          <button
            onClick={fetchLiveGames}
            className="bg-gray-600 hover:bg-gray-700 text-white font-medium py-2 px-4 rounded-md transition duration-200"
          >
            새로고침
          </button>
        </div>
      </div>
    </div>
  );
};

export default LiveGames;

