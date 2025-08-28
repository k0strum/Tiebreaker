import { useEffect, useState } from 'react'
import axios from '../utils/axios'

export default function GameSchedule() {
  const [date, setDate] = useState(() => new Date().toISOString().slice(0, 10))
  const [games, setGames] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const fetchGames = async (targetDate) => {
    try {
      setLoading(true)
      setError('')
      const res = await axios.get(`/games?date=${targetDate}`)
      setGames(res.data)
    } catch (e) {
      setError(String(e))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchGames(date)
  }, [])

  // 경기 상태에 따른 배지 색상
  const getStatusBadge = (game) => {
    const statusCode = game.statusCode;
    const statusInfo = game.statusInfo;

    // statusInfo가 있으면 우선 사용
    if (statusInfo) {
      let color = 'bg-gray-100 text-gray-800';

      if (statusInfo.includes('취소')) {
        color = 'bg-red-100 text-red-800';
      } else if (statusInfo.includes('연기')) {
        color = 'bg-yellow-100 text-yellow-800';
      } else if (statusInfo.includes('경기전') || statusInfo.includes('예정')) {
        color = 'bg-blue-100 text-blue-800';
      } else if (statusInfo.includes('회')) {
        color = 'bg-red-100 text-red-800';
      }

      return { text: statusInfo, color };
    }

    // statusInfo가 없으면 기존 로직 사용
    const statusMap = {
      'BEFORE': { text: '예정', color: 'bg-blue-100 text-blue-800' },
      'READY': { text: '경기 준비중', color: 'bg-green-100 text-green-800' },
      'LIVE': { text: '진행중', color: 'bg-red-100 text-red-800' },
      'RESULT': { text: '종료', color: 'bg-gray-100 text-gray-800' },
      'POSTPONED': { text: '연기', color: 'bg-yellow-100 text-yellow-800' },
      'CANCELLED': { text: '취소', color: 'bg-red-100 text-red-800' }
    };
    return statusMap[statusCode] || { text: statusCode, color: 'bg-gray-100 text-gray-800' };
  };

  // 시간 포맷팅
  const formatTime = (dateTime) => {
    if (!dateTime) return '';
    return dateTime.slice(11, 16);
  };

  // 날짜 포맷팅
  const formatDate = (gameDate) => {
    if (!gameDate) return '';
    const date = new Date(gameDate);
    const days = ['일', '월', '화', '수', '목', '금', '토'];
    return `${date.getMonth() + 1}월 ${date.getDate()}일 (${days[date.getDay()]})`;
  };

  // 로딩 컴포넌트
  const LoadingCard = () => (
    <div className="bg-white rounded-lg shadow-md p-6">
      <div className="animate-pulse">
        <div className="h-6 bg-gray-200 rounded mb-4"></div>
        <div className="space-y-3">
          <div className="h-20 bg-gray-200 rounded"></div>
          <div className="h-20 bg-gray-200 rounded"></div>
          <div className="h-20 bg-gray-200 rounded"></div>
        </div>
      </div>
    </div>
  );

  // 에러 컴포넌트
  const ErrorCard = () => (
    <div className="bg-white rounded-lg shadow-md p-6">
      <div className="text-center py-8">
        <div className="text-red-500 text-4xl mb-4">⚠️</div>
        <p className="text-lg text-gray-600 mb-4">경기 일정을 불러오는데 실패했습니다.</p>
        <button
          onClick={() => fetchGames(date)}
          className="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition-colors"
        >
          재시도
        </button>
      </div>
    </div>
  );

  // 경기 카드 컴포넌트
  const GameCard = ({ game }) => {
    const statusBadge = getStatusBadge(game);
    const isLive = game.statusCode === 'LIVE' || (game.statusInfo && game.statusInfo.includes('회'));
    const isFinished = game.statusCode === 'FINISHED' || game.statusCode === 'RESULT';

    return (
      <div className={`bg-white rounded-lg shadow-md p-6 transition-all hover:shadow-lg ${isLive && !isFinished ? 'ring-2 ring-red-500' : ''}`}>
        {/* 경기 헤더 */}
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-3">
            <span className="text-2xl">⚾</span>
            <div>
              <h3 className="text-lg font-semibold text-gray-800">
                {formatDate(game.gameDate)}
              </h3>
              <p className="text-sm text-gray-500">
                {formatTime(game.gameDateTime)} · {game.stadium}
              </p>
            </div>
          </div>
          <span className={`px-3 py-1 rounded-full text-sm font-medium ${statusBadge.color}`}>
            {statusBadge.text}
          </span>
        </div>

        {/* 경기 정보 */}
        <div className="bg-gray-50 rounded-lg p-4 mb-4">
          <div className="flex items-center justify-between">
            {/* 원정팀 */}
            <div className="flex-1 text-center">
              <div className="text-lg font-bold text-gray-800 mb-1">
                {game.awayTeamName}
              </div>
              <div className={`text-2xl font-bold ${isFinished || isLive ? 'text-blue-600' : 'text-gray-400'}`}>
                {game.awayTeamScore ?? '-'}
              </div>
              {game.awayStartingPitcher && (
                <div className="text-xs text-gray-500 mt-1">
                  선발: {game.awayStartingPitcher}
                </div>
              )}
            </div>

            {/* VS */}
            <div className="mx-4 text-gray-400 font-bold">
              VS
            </div>

            {/* 홈팀 */}
            <div className="flex-1 text-center">
              <div className="text-lg font-bold text-gray-800 mb-1">
                {game.homeTeamName}
              </div>
              <div className={`text-2xl font-bold ${isFinished || isLive ? 'text-blue-600' : 'text-gray-400'}`}>
                {game.homeTeamScore ?? '-'}
              </div>
              {game.homeStartingPitcher && (
                <div className="text-xs text-gray-500 mt-1">
                  선발: {game.homeStartingPitcher}
                </div>
              )}
            </div>
          </div>
        </div>

        {/* 추가 정보 */}
        <div className="flex items-center justify-between text-sm text-gray-600">
          <div className="flex items-center space-x-4">
            {game.broadChannel && (
              <div className="flex items-center space-x-1">
                <span className="text-red-500">📺</span>
                <span>{game.broadChannel}</span>
              </div>
            )}
            {game.attendance && (
              <div className="flex items-center space-x-1">
                <span className="text-green-500">👥</span>
                <span>{game.attendance.toLocaleString()}명</span>
              </div>
            )}
          </div>
          {isLive && !isFinished && (
            <div className="flex items-center space-x-1 text-red-500">
              <div className="w-2 h-2 bg-red-500 rounded-full animate-pulse"></div>
              <span className="font-medium">LIVE</span>
            </div>
          )}
          {isFinished && (
            <div className="flex items-center space-x-1 text-gray-500">
              <span className="font-medium">종료</span>
            </div>
          )}
        </div>
      </div>
    );
  };

  return (
    <div className="container mx-auto p-8">
      <h1 className="text-3xl font-bold text-blue-600 mb-6">📅 경기 일정</h1>

      {/* 날짜 선택 영역 */}
      <div className="bg-white rounded-lg shadow-md p-6 mb-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-semibold text-gray-800">날짜 선택</h2>
          <div className="text-sm text-gray-500">
            {formatDate(date)}
          </div>
        </div>

        <div className="flex items-center space-x-4">
          <input
            type="date"
            value={date}
            onChange={(e) => {
              setDate(e.target.value);
              fetchGames(e.target.value);
            }}
            className="border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
          />
          <button
            onClick={() => {
              const today = new Date().toISOString().slice(0, 10);
              setDate(today);
              fetchGames(today);
            }}
            disabled={loading}
            className="bg-gray-500 text-white px-4 py-2 rounded-lg hover:bg-gray-600 transition-colors disabled:opacity-50"
          >
            오늘
          </button>
        </div>
      </div>

      {/* 경기 목록 */}
      <div className="space-y-6">
        {loading && <LoadingCard />}

        {error && <ErrorCard />}

        {!loading && !error && games.length === 0 && (
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="text-center py-8">
              <div className="text-gray-400 text-4xl mb-4">📅</div>
              <p className="text-lg text-gray-600">해당 날짜에 예정된 경기가 없습니다.</p>
            </div>
          </div>
        )}

        {!loading && !error && games.length > 0 && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {games.map((game) => (
              <GameCard key={game.gameId} game={game} />
            ))}
          </div>
        )}
      </div>

      {/* 정보 패널 */}
      <div className="mt-6 bg-blue-50 rounded-lg p-4">
        <h3 className="font-semibold text-blue-800 mb-2">ℹ️ 경기 정보 안내</h3>
        <div className="text-sm text-blue-700 space-y-1">
          <p>• <strong>예정</strong>: 아직 시작하지 않은 경기</p>
          <p>• <strong>진행중</strong>: 현재 진행 중인 경기 (빨간 테두리 표시)</p>
          <p>• <strong>종료</strong>: 완료된 경기 (스코어 표시)</p>
          <p>• <strong>연기/취소</strong>: 일정이 변경된 경기</p>
        </div>
      </div>
    </div>
  )
}


