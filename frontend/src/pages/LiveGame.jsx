import React, { useState, useEffect, useRef } from 'react';
import { Client as StompClient } from '@stomp/stompjs';
import { useAuth } from '../contexts/AuthContext.jsx';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';

const LiveGame = () => {
  const { gameId } = useParams();
  const navigate = useNavigate();
  const { isLoggedIn, nickname } = useAuth() || { isLoggedIn: false, nickname: '' };
  const [gameInfo, setGameInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [sseConnected, setSseConnected] = useState(false);
  const [commentaries, setCommentaries] = useState([]);
  const [cmtConnected, setCmtConnected] = useState(false);
  const commentaryEndRef = useRef(null);
  const commentaryContainerRef = useRef(null);
  const [chatMessages, setChatMessages] = useState([]);
  const [chatInput, setChatInput] = useState('');
  const chatEndRef = useRef(null);
  const stompRef = useRef(null);

  const scrollCommentaryToBottom = () => {
    // DOM 업데이트 이후 두 번 보정하여 완전 하단 고정
    requestAnimationFrame(() => {
      if (commentaryContainerRef.current) {
        commentaryContainerRef.current.scrollTop = commentaryContainerRef.current.scrollHeight;
        // 일부 브라우저에서 한 줄 위로 고정되는 현상 보정
        setTimeout(() => {
          if (commentaryContainerRef.current) {
            commentaryContainerRef.current.scrollTop = commentaryContainerRef.current.scrollHeight;
          }
        }, 0);
      }
    });
  };

  useEffect(() => {
    // 초기 데이터 로드
    fetchGameInfo();
    fetchCommentary();

    // SSE 연결
    const eventSource = new EventSource(`/api/live-games/${gameId}/subscribe`);
    const commentarySource = new EventSource(`/api/sse/games/${gameId}/livegame`);

    eventSource.onopen = () => {
      console.log('SSE 연결 성공');
      setSseConnected(true);
    };

    eventSource.onmessage = (event) => {
      console.log('SSE 메시지 수신:', event);
    };

    eventSource.addEventListener('init', (event) => {
      console.log('SSE 초기화 완료:', event.data);
    });

    eventSource.addEventListener('live-game-info', (event) => {
      try {
        const data = JSON.parse(event.data);
        console.log('실시간 경기 정보 수신:', data);
        setGameInfo(data);
        setLoading(false);
      } catch (err) {
        console.error('SSE 데이터 파싱 오류:', err);
      }
    });

    // Commentary SSE
    commentarySource.onopen = () => {
      console.log('Commentary SSE 연결 성공');
      setCmtConnected(true);
    };
    commentarySource.addEventListener('init', (event) => {
      console.log('Commentary SSE 초기화 완료:', event.data);
    });
    commentarySource.addEventListener('livegame', (event) => {
      try {
        const data = JSON.parse(event.data);
        console.log('Commentary SSE 데이터 수신:', data);
        setCommentaries((prev) => (
          [...prev, {
            ts: data.ts,
            text: data.text,
            inning: data.inning,
            half: data.half,
            severity: data.severity
          }].slice(-200)
        ));
        scrollCommentaryToBottom();
      } catch (err) {
        console.error('Commentary SSE 데이터 파싱 오류:', err);
      }
    });

    eventSource.onerror = (error) => {
      console.error('SSE 연결 오류:', error);
      setSseConnected(false);
    };
    commentarySource.onerror = (error) => {
      console.error('Commentary SSE 연결 오류:', error);
      setCmtConnected(false);
    };

    return () => {
      eventSource.close();
      commentarySource.close();
    };
  }, [gameId]);

  useEffect(() => {
    // STOMP 네이티브 WebSocket 연결 (SockJS 이슈 회피)
    const wsProtocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const brokerURL = `${wsProtocol}://localhost:8080/ws-native`;
    const client = new StompClient({
      brokerURL,
      reconnectDelay: 3000,
      debug: (msg) => console.log('[STOMP]', msg)
    });

    client.onConnect = () => {
      stompRef.current = client;
      // 구독
      client.subscribe(`/topic/chat.${gameId}`, (message) => {
        try {
          const body = JSON.parse(message.body);
          setChatMessages((prev) => [...prev, body].slice(-200));
          requestAnimationFrame(() => chatEndRef.current?.scrollIntoView({ behavior: 'smooth' }));
        } catch (e) {
          console.error('채팅 메시지 파싱 오류', e);
        }
      });
    };

    client.activate();
    return () => {
      try { client.deactivate(); } catch { }
      stompRef.current = null;
    };
  }, [gameId]);

  const sendChat = () => {
    const content = chatInput.trim();
    if (!content || !stompRef.current) return;
    // 닉네임 구성: 로그인 사용자 → nickname, 비로그인 → Guest-XXXX
    const guestId = (() => {
      let id = localStorage.getItem('guestId');
      if (!id) {
        id = Math.floor(1000 + Math.random() * 9000).toString();
        localStorage.setItem('guestId', id);
      }
      return id;
    })();

    const displayName = isLoggedIn && nickname ? nickname : `Guest-${guestId}`;

    const payload = {
      gameId,
      sender: displayName,
      content,
      ts: Date.now(),
    };
    stompRef.current.publish({ destination: '/app/chat.send', body: JSON.stringify(payload) });
    setChatInput('');
  };

  const fetchGameInfo = async () => {
    try {
      const response = await axios.get(`/api/live-games/${gameId}`);
      setGameInfo(response.data);
      setLoading(false);
    } catch (err) {
      console.error('경기 정보 조회 실패:', err);
      setError('경기 정보를 불러오는데 실패했습니다.');
      setLoading(false);
    }
  };

  const fetchCommentary = async () => {
    try {
      const res = await axios.get(`/api/games/${gameId}/livegame`, { params: { page: 0, size: 50 } });
      const items = res.data.content || [];
      // 서버는 최신 내림차순으로 반환 -> 화면은 오래된 → 최신(아래) 순으로 변환
      const mapped = items
        .map(it => ({ ts: it.ts, text: it.text, inning: it.inning, half: it.half, severity: it.severity }))
        .reverse();
      setCommentaries(mapped);
    } catch (e) {
      console.error('해설 초기 로드 실패:', e);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'READY':
        return <span className="px-3 py-1 text-sm font-semibold bg-yellow-100 text-yellow-800 rounded-full">예정</span>;
      case 'LIVE':
        return <span className="px-3 py-1 text-sm font-semibold bg-red-100 text-red-800 rounded-full animate-pulse">LIVE</span>;
      case 'FINISHED':
        return <span className="px-3 py-1 text-sm font-semibold bg-gray-100 text-gray-800 rounded-full">종료</span>;
      default:
        return <span className="px-3 py-1 text-sm font-semibold bg-gray-100 text-gray-800 rounded-full">{status}</span>;
    }
  };

  const formatInning = (inning, half) => {
    if (inning === 0) return '경기전';
    const halfText = half === 'T' ? '초' : '말';
    return `${inning}회${halfText}`;
  };

  const renderBases = (bases) => {
    if (!bases || !Array.isArray(bases)) return null;

    return (
      <div className="flex space-x-2">
        {bases.map((occupied, index) => (
          <div
            key={index}
            className={`w-4 h-4 rounded-full border-2 ${occupied
              ? 'bg-green-500 border-green-600'
              : 'bg-gray-200 border-gray-300'
              }`}
            title={`${index + 1}루`}
          />
        ))}
      </div>
    );
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-4 text-gray-600">경기 정보를 불러오는 중...</p>
            {sseConnected && (
              <p className="mt-2 text-sm text-green-600">실시간 연결됨</p>
            )}
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
              <p>{error}</p>
            </div>
            <button
              onClick={() => navigate('/live-games')}
              className="mt-4 bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-md"
            >
              실시간 경기 목록으로 돌아가기
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (!gameInfo) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <p className="text-gray-600">경기 정보를 찾을 수 없습니다.</p>
            <button
              onClick={() => navigate('/live-games')}
              className="mt-4 bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-md"
            >
              실시간 경기 목록으로 돌아가기
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* 헤더 */}
        <div className="mb-8">
          <div className="flex items-center justify-between">
            <div>
              <button
                onClick={() => navigate('/live-games')}
                className="text-blue-600 hover:text-blue-800 mb-4 flex items-center"
              >
                <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
                실시간 경기 목록
              </button>
              <h1 className="text-3xl font-bold text-gray-900">
                {gameInfo.awayTeam} vs {gameInfo.homeTeam}
              </h1>
            </div>
            <div className="flex items-center space-x-4">
              {getStatusBadge(gameInfo.status)}
              {sseConnected && (
                <span className="px-2 py-1 text-xs font-semibold bg-green-100 text-green-800 rounded-full">
                  실시간 연결됨
                </span>
              )}
            </div>
          </div>
          <p className="text-lg text-gray-600 mt-2">{formatInning(gameInfo.inning, gameInfo.half)}</p>
        </div>

        {/* 스코어보드 */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="grid grid-cols-2 gap-8">
            {/* 원정팀 */}
            <div className="text-center">
              <h3 className="text-xl font-bold text-gray-900 mb-2">{gameInfo.awayTeam}</h3>
              <div className="text-4xl font-bold text-blue-600">{gameInfo.awayScore}</div>
            </div>

            {/* 홈팀 */}
            <div className="text-center">
              <h3 className="text-xl font-bold text-gray-900 mb-2">{gameInfo.homeTeam}</h3>
              <div className="text-4xl font-bold text-red-600">{gameInfo.homeScore}</div>
            </div>
          </div>
        </div>

        {/* 실시간 정보 */}
        {gameInfo.status === 'LIVE' && (
          <div className="bg-white rounded-lg shadow-md p-6 mb-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">현재 상황</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <h4 className="font-medium text-gray-700 mb-2">타자 정보</h4>
                <p className="text-sm text-gray-600">{gameInfo.currentBatter}</p>
              </div>
              <div>
                <h4 className="font-medium text-gray-700 mb-2">투수 정보</h4>
                <p className="text-sm text-gray-600">{gameInfo.currentPitcher}</p>
              </div>
              <div>
                <h4 className="font-medium text-gray-700 mb-2">베이스 상황</h4>
                <div className="flex items-center space-x-2">
                  {renderBases(gameInfo.bases)}
                  <span className="text-sm text-gray-600">(1루, 2루, 3루)</span>
                </div>
              </div>
              <div>
                <h4 className="font-medium text-gray-700 mb-2">볼-스트라이크</h4>
                <p className="text-sm text-gray-600">{gameInfo.balls}-{gameInfo.strikes}</p>
              </div>
              <div>
                <h4 className="font-medium text-gray-700 mb-2">아웃</h4>
                <p className="text-sm text-gray-600">{gameInfo.outs}개</p>
              </div>
            </div>
          </div>
        )}

        {/* 실시간 문자중계 */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900">문자중계</h3>
            <div className="flex items-center space-x-2">
              {cmtConnected && (
                <span className="px-2 py-1 text-xs font-semibold bg-green-100 text-green-800 rounded-full">수신 중</span>
              )}
            </div>
          </div>
          <div className="max-h-80 overflow-y-auto space-y-3 pr-2" ref={commentaryContainerRef}>
            {commentaries.length === 0 && (
              <p className="text-sm text-gray-500">아직 도착한 해설이 없습니다.</p>
            )}
            {commentaries.map((c, idx) => (
              <div key={idx} className="text-sm text-gray-800">
                <span className="text-gray-500 mr-2">{c.inning > 0 ? `${c.inning}회${c.half === 'T' ? '초' : '말'}` : '경기전'}</span>
                <span>{c.text}</span>
              </div>
            ))}
            <div ref={commentaryEndRef} />
          </div>
        </div>

        {/* 실시간 채팅 */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-900">채팅</h3>
          </div>
          <div className="max-h-80 overflow-y-auto space-y-3 pr-2">
            {chatMessages.map((m, idx) => (
              <div key={idx} className={`text-sm ${m.type === 'NOTIFICATION' ? 'text-center' : 'text-gray-800'}`}>
                {m.type === 'NOTIFICATION' ? (
                  <div className="bg-blue-50 border border-blue-200 rounded-lg px-3 py-2">
                    <span className="text-blue-800 font-medium">{m.content}</span>
                  </div>
                ) : (
                  <>
                    <span className="font-semibold text-gray-700 mr-2">{m.sender ?? '익명'}</span>
                    <span>{m.content}</span>
                  </>
                )}
              </div>
            ))}
            <div ref={chatEndRef} />
          </div>
          <div className="mt-4 flex items-center space-x-2">
            <input
              type="text"
              value={chatInput}
              onChange={(e) => setChatInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && sendChat()}
              className="flex-1 border rounded-md px-3 py-2 text-sm"
              placeholder="메시지를 입력하세요"
              maxLength={300}
            />
            <button
              onClick={sendChat}
              className="bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium px-4 py-2 rounded-md"
            >전송</button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LiveGame;

