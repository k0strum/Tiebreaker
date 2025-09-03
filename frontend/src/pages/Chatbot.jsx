import { useEffect, useRef, useState } from 'react'

// 백엔드 베이스 URL (환경변수 우선)
const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const resolveLogoUrl = (url) => {
  if (!url) return url
  // 백엔드가 "/images/..." 같은 상대 경로를 내려줄 때 절대 경로로 변환
  if (url.startsWith('/')) return `${apiBase}${url}`
  return url
}

function Chatbot() {
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [isConnected, setIsConnected] = useState(false)
  const [pendingReqId, setPendingReqId] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const wsRef = useRef(null)
  const endRef = useRef(null)

  // 도구 입력 상태
  const [playerName, setPlayerName] = useState('')
  const [date, setDate] = useState('')

  // 에러 토스트 자동 제거
  useEffect(() => {
    if (error) {
      const timer = setTimeout(() => setError(null), 5000)
      return () => clearTimeout(timer)
    }
  }, [error])

  useEffect(() => {
    // 이미 연결된 경우 중복 연결 방지
    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      console.log('이미 WebSocket이 연결되어 있습니다.')
      return
    }

    const connectWebSocket = () => {
      console.log('WebSocket 연결 시도 중...')
      const ws = new WebSocket('ws://localhost:8080/mcp')

      ws.onopen = () => {
        console.log('WebSocket 연결 성공!')
        setIsConnected(true)
        appendSystem('안녕하세요! KBO 야구 정보를 도와드리는 AI 어시스턴트입니다. 🏟️')
        // 도구 목록 미리 로드 (사용자에게는 보이지 않음)
        const reqId = `list-${Date.now()}`
        setPendingReqId(reqId)
        ws.send(JSON.stringify({ type: 'tools/list', requestId: reqId }))
      }

      ws.onmessage = (e) => {
        try {
          const data = JSON.parse(e.data)
          console.log('WebSocket 메시지 수신:', data)

          // 시스템 메시지나 도구 목록은 사용자에게 보이지 않게 처리
          if (data.type === 'hello' || (data.type === 'tool/result' && Array.isArray(data.content))) {
            return
          }

          appendServer(data)
          setLoading(false)
        } catch (err) {
          console.error('WebSocket 메시지 파싱 오류:', err)
          setLoading(false)
        }
      }

      ws.onerror = (error) => {
        console.error('WebSocket 오류:', error)
        setError('WebSocket 연결 오류가 발생했습니다.')
        setLoading(false)
      }

      ws.onclose = (event) => {
        console.log('WebSocket 연결 종료:', event.code, event.reason)
        setIsConnected(false)
        setLoading(false)

        // 연결이 비정상적으로 종료된 경우 재연결 시도
        if (event.code !== 1000) {
          console.log('3초 후 재연결 시도...')
          setTimeout(() => {
            if (!isConnected) {
              connectWebSocket()
            }
          }, 3000)
        }
      }

      wsRef.current = ws
    }

    connectWebSocket()

    return () => {
      if (wsRef.current) {
        console.log('WebSocket 연결 정리 중...')
        try {
          wsRef.current.close(1000, 'Component unmounting')
        } catch (error) {
          console.error('WebSocket 정리 오류:', error)
        }
      }
    }
  }, [])

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  // 고유 ID 생성을 위한 카운터
  const messageIdCounter = useRef(0)

  const generateUniqueId = () => {
    messageIdCounter.current += 1
    return `${Date.now()}-${messageIdCounter.current}`
  }

  const appendSystem = (text) => {
    setMessages((prev) => [...prev, { id: generateUniqueId(), role: 'system', text }])
  }
  const appendUser = (text) => {
    setMessages((prev) => [...prev, { id: generateUniqueId(), role: 'user', text }])
  }
  const appendServer = (payload) => {
    setMessages((prev) => [...prev, { id: generateUniqueId(), role: 'server', payload }])
  }

  // 날짜 형식 검증
  const validateDate = (dateStr) => {
    if (!dateStr) return true
    const dateRegex = /^\d{4}-\d{2}-\d{2}$/
    if (!dateRegex.test(dateStr)) return false
    const date = new Date(dateStr)
    return !isNaN(date.getTime())
  }

  const sendFreeform = () => {
    if (!input.trim() || !isConnected) return
    appendUser(input.trim())
    setInput('')
  }

  // MCP 도구 호출 유틸
  const callTool = (toolName, args = {}) => {
    if (!isConnected || !wsRef.current) return
    setLoading(true)
    const reqId = `${toolName}-${Date.now()}`
    setPendingReqId(reqId)
    wsRef.current.send(JSON.stringify({
      type: 'tool/call',
      requestId: reqId,
      toolName,
      arguments: args
    }))
  }

  // 핸들러들
  const handleGetTeamRanking = () => {
    callTool('getTeamRanking', {})
  }
  const handleGetGameSchedule = () => {
    if (date && !validateDate(date.trim())) {
      setError('날짜 형식이 올바르지 않습니다. (YYYY-MM-DD)')
      return
    }
    const args = {}
    if (date && date.trim()) args.date = date.trim()
    callTool('getGameSchedule', args)
  }
  const handleGetPlayerStats = () => {
    if (!playerName.trim()) {
      setError('선수 이름을 입력해주세요.')
      return
    }
    if (playerName.trim().length < 2) {
      setError('선수 이름은 2글자 이상 입력해주세요.')
      return
    }
    callTool('getPlayerStats', { playerName: playerName.trim() })
  }

  // 로딩 스피너 컴포넌트
  const LoadingSpinner = () => (
    <div className="inline-flex items-center">
      <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
      </svg>
      처리중...
    </div>
  )

  // 에러 토스트 컴포넌트
  const ErrorToast = () => {
    if (!error) return null
    return (
      <div className="fixed top-4 right-4 bg-red-500 text-white px-6 py-3 rounded-lg shadow-lg z-50 max-w-sm">
        <div className="flex items-center">
          <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
          </svg>
          {error}
        </div>
      </div>
    )
  }

  // 렌더링 유틸: 팀 순위
  const TeamRanksView = ({ content }) => {
    const ranks = content?.ranks || []
    if (!Array.isArray(ranks) || ranks.length === 0) {
      return <div className="text-sm text-gray-600">팀 순위 데이터가 없습니다.</div>
    }
    return (
      <div className="space-y-2">
        <div className="font-semibold text-gray-800">🏆 현재 시즌 팀 순위</div>
        <div className="overflow-x-auto">
          <table className="min-w-full text-sm">
            <thead>
              <tr className="text-left text-gray-600">
                <th className="py-1 pr-3">순위</th>
                <th className="py-1 pr-3">팀</th>
                <th className="py-1 pr-3">승률</th>
                <th className="py-1 pr-3">경기차</th>
                <th className="py-1 pr-3">연속</th>
              </tr>
            </thead>
            <tbody>
              {ranks.map((r, idx) => (
                <tr key={idx} className={`border-t ${idx < 3 ? 'bg-yellow-50 font-medium' : ''}`}>
                  <td className="py-1 pr-3">{r.rank}</td>
                  <td className="py-1 pr-3 flex items-center gap-2">
                    {r.teamLogoUrl ? <img src={resolveLogoUrl(r.teamLogoUrl)} alt={r.teamName} className="w-6 h-6" /> : null}
                    <span>{r.teamName}</span>
                  </td>
                  <td className="py-1 pr-3">{r.winRate}</td>
                  <td className="py-1 pr-3">{r.gameBehind}</td>
                  <td className="py-1 pr-3">{r.streak}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {content.message ? <div className="text-gray-600 text-sm">{content.message}</div> : null}
      </div>
    )
  }

  // 렌더링 유틸: 경기 일정
  const SchedulesView = ({ content }) => {
    const schedules = content?.schedules || []
    const date = content?.date
    if (!Array.isArray(schedules) || schedules.length === 0) {
      return (
        <div className="space-y-2">
          <div className="font-semibold text-gray-800">📅 경기 일정</div>
          <div className="text-sm text-gray-600">
            {date ? `${date} 경기 일정이 없습니다.` : '오늘 경기 일정이 없습니다.'}
          </div>
        </div>
      )
    }
    return (
      <div className="space-y-2">
        <div className="font-semibold text-gray-800">📅 경기 일정</div>
        <div className="space-y-2">
          {schedules.map((s, idx) => (
            <div key={idx} className="border rounded-lg p-3 bg-gray-50">
              <div className="flex justify-between items-center mb-2">
                <div className="flex items-center gap-2">
                  <span className="text-sm font-medium">
                    {s.homeTeamScore !== null && s.awayTeamScore !== null ? (
                      `${s.homeTeamName} ${s.homeTeamScore} vs ${s.awayTeamScore} ${s.awayTeamName}`
                    ) : (
                      <>
                        <span>{s.homeTeamName}</span>
                        <span className="text-xs text-gray-500">vs</span>
                        <span>{s.awayTeamName}</span>
                      </>
                    )}
                  </span>
                </div>
                <div className="text-xs text-gray-500">
                  {s.gameDateTime ? new Date(s.gameDateTime).toLocaleTimeString('ko-KR', {
                    hour: '2-digit',
                    minute: '2-digit'
                  }) : '-'}
                </div>
              </div>
              <div className="flex justify-between items-center text-xs text-gray-600">
                <div className="flex items-center gap-4">
                  {s.stadium && <span>🏟️ {s.stadium}</span>}
                  {s.broadChannel && <span>📺 {s.broadChannel}</span>}
                </div>
                <div className="flex items-center gap-2">
                  {s.homeStarterName && <span>🏠 {s.homeStarterName}</span>}
                  {s.awayStarterName && <span>✈️ {s.awayStarterName}</span>}
                </div>
              </div>
              <div className="mt-2 pt-2 border-t border-gray-200">
                <div className="flex justify-between items-center text-xs">
                  <div className="flex items-center gap-2">
                    <span className={`px-2 py-1 rounded ${s.statusCode === 'BEFORE' ? 'bg-blue-100 text-blue-800' :
                      s.statusCode === 'LIVE' ? 'bg-red-100 text-red-800' :
                        s.statusCode === 'RESULT' ? 'bg-green-100 text-green-800' :
                          s.statusCode === 'END' ? 'bg-gray-100 text-gray-800' :
                            'bg-yellow-100 text-yellow-800'
                      }`}>
                      {s.statusCode === 'RESULT' ? '경기완료' :
                        s.statusCode === 'BEFORE' && s.statusInfo === '경기취소' ? '경기취소' :
                          s.statusCode === 'LIVE' ? '경기중' :
                            s.statusInfo || s.statusCode}
                    </span>

                    {s.statusCode === 'LIVE' && s.statusInfo && (
                      <span className="text-gray-600 bg-gray-100 px-2 py-1 rounded">
                        {s.statusInfo}
                      </span>
                    )}
                    {s.winner && s.winner !== 'DRAW' && (
                      <span className={`px-2 py-1 rounded text-white text-xs ${s.winner === 'HOME' ? 'bg-blue-600' : 'bg-red-600'
                        }`}>
                        {s.winner === 'HOME' ? '홈팀승' : '원정팀승'}
                      </span>
                    )}
                  </div>
                  <span className="text-gray-500">ID: {s.gameId}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
        {content.message ? <div className="text-gray-600 text-sm">{content.message}</div> : null}
      </div>
    )
  }

  // 렌더링 유틸: 선수 스탯
  const PlayerStatsView = ({ content }) => {
    // 후보 리스트인 경우
    if (content.candidates) {
      return (
        <div className="space-y-2">
          <div className="font-semibold text-gray-800">⚾ 선수 후보</div>
          <div className="text-sm text-gray-600 mb-2">{content.message}</div>
          <div className="space-y-1">
            {content.candidates.map((c, idx) => (
              <button
                key={idx}
                onClick={() => callTool('getPlayerStats', { playerId: c.playerId })}
                className="w-full text-left p-2 border rounded hover:bg-blue-50 transition-colors"
              >
                <div className="font-medium">{c.playerName}</div>
                <div className="text-xs text-gray-500">{c.teamName} • {c.position}</div>
              </button>
            ))}
          </div>
        </div>
      )
    }

    // 선수 상세 정보인 경우
    const stats = content?.stats
    if (!stats) {
      return (
        <div className="space-y-2">
          <div className="font-semibold text-gray-800">⚾ 선수 정보</div>
          <div className="text-sm text-gray-600">{content.message || '선수 정보를 찾을 수 없습니다.'}</div>
        </div>
      )
    }

    return (
      <div className="space-y-2">
        <div className="font-semibold text-gray-800">
          ⚾ {content.playerName} ({content.teamName})
        </div>
        <div className="text-sm text-gray-600 mb-2">{stats.type}</div>
        <div className="grid grid-cols-2 md:grid-cols-3 gap-2 text-sm">
          {Object.entries(stats).filter(([k]) => k !== 'type').map(([k, v]) => (
            <div key={k} className="border rounded p-2 bg-gray-50">
              <div className="text-gray-500 text-xs">{k}</div>
              <div className="font-medium">{String(v)}</div>
            </div>
          ))}
        </div>
        {content.message ? <div className="text-gray-600 text-sm">{content.message}</div> : null}
      </div>
    )
  }

  const renderPayloadPretty = (payload) => {
    // 서버 통신 기본 래핑 형식: { type, requestId, content, ... }
    const c = payload?.content ?? payload
    if (!c || typeof c !== 'object') return null

    // 오류 응답 공통 처리
    if (c.error && !c.ranks && !c.schedules && !c.stats && !c.candidates) {
      return <div className="text-red-700 bg-red-50 border-l-4 border-red-500 p-3 text-sm">{c.error}{c.detail ? ` - ${c.detail}` : ''}</div>
    }

    if (c.ranks) return <TeamRanksView content={c} />
    if (c.schedules || c.date) return <SchedulesView content={c} />
    if (c.stats || c.candidates || c.playerName) return <PlayerStatsView content={c} />

    return null
  }

  const renderMessage = (msg) => {
    if (msg.role === 'system') {
      return (
        <div className="text-center text-gray-500 bg-blue-50 p-3 rounded-lg border-l-4 border-blue-500">
          {msg.text}
        </div>
      )
    }
    if (msg.role === 'user') {
      return (
        <div className="flex items-start space-x-3 justify-end">
          <div className="flex-1 text-right">
            <p className="text-white bg-blue-600 p-3 rounded-lg inline-block">{msg.text}</p>
          </div>
          <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center text-white text-sm font-bold">나</div>
        </div>
      )
    }
    // server
    const payload = msg.payload
    const pretty = renderPayloadPretty(payload)
    return (
      <div className="flex items-start space-x-3">
        <div className="w-8 h-8 bg-orange-500 rounded-full flex items-center justify-center text-white text-sm">AI</div>
        <div className="flex-1">
          {pretty ? (
            <div className="text-gray-700 bg-orange-50 p-3 rounded-lg border-l-4 border-orange-500 overflow-x-auto">
              {pretty}
            </div>
          ) : (
            <div className="text-gray-700 bg-orange-50 p-3 rounded-lg border-l-4 border-orange-500 overflow-x-auto">
              <pre className="whitespace-pre-wrap break-words text-sm">{JSON.stringify(payload, null, 2)}</pre>
            </div>
          )}
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto p-8 bg-gray-50 min-h-screen">
      <ErrorToast />
      <h1 className="text-3xl font-bold text-orange-600 mb-6">🤖 KBO AI 어시스턴트</h1><br />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 좌측: 채팅 영역 */}
        <div className="lg:col-span-2 bg-white rounded-lg shadow-md h-[600px] flex flex-col">
          <div className="bg-orange-600 text-white p-4 rounded-t-lg flex items-center justify-between">
            <div className="font-semibold">KBO 야구 정보 도우미</div>
            <div className={`text-xs ${isConnected ? 'text-green-200' : 'text-red-200'}`}>
              {isConnected ? '🟢 연결됨' : '🔴 연결 안 됨'}
            </div>
          </div>

          <div className="flex-1 p-4 overflow-y-auto space-y-3 bg-gray-50">
            {messages.map((m) => (
              <div key={m.id}>{renderMessage(m)}</div>
            ))}
            <div ref={endRef} />
          </div>

          <div className="p-4 border-t bg-white">
            <div className="flex space-x-2">
              <input
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') sendFreeform() }}
                placeholder="야구 관련 질문을 자유롭게 입력하세요..."
                className="flex-1 border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-orange-500"
                disabled={!isConnected || loading}
              />
              <button
                onClick={sendFreeform}
                disabled={!isConnected || !input.trim() || loading}
                className="bg-orange-600 text-white px-6 py-2 rounded-lg hover:bg-orange-700 transition-colors disabled:bg-gray-400 min-w-[80px]"
              >
                {loading ? <LoadingSpinner /> : '전송'}
              </button>
            </div>
          </div>
        </div>

        {/* 우측: 도구 패널 */}
        <div className="bg-white rounded-lg shadow-md p-4 space-y-4">
          <h2 className="font-semibold text-gray-800">⚡ 빠른 조회</h2>

          <div className="space-y-2">
            <div className="text-sm font-medium text-gray-700">🏆 팀 순위</div>
            <button
              onClick={handleGetTeamRanking}
              disabled={!isConnected || loading}
              className="w-full bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 disabled:bg-gray-400 transition-colors"
            >
              {loading && pendingReqId?.includes('getTeamRanking') ? <LoadingSpinner /> : '현재 순위 보기'}
            </button>
          </div>

          <div className="space-y-2">
            <div className="text-sm font-medium text-gray-700">📅 경기 일정</div>
            <input
              type="text"
              value={date}
              onChange={(e) => setDate(e.target.value)}
              placeholder="YYYY-MM-DD (비우면 오늘)"
              className="w-full border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              disabled={!isConnected || loading}
            />
            <button
              onClick={handleGetGameSchedule}
              disabled={!isConnected || loading}
              className="w-full bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 disabled:bg-gray-400 transition-colors"
            >
              {loading && pendingReqId?.includes('getGameSchedule') ? <LoadingSpinner /> : '일정 확인'}
            </button>
          </div>

          <div className="space-y-2">
            <div className="text-sm font-medium text-gray-700">⚾ 선수 조회</div>
            <input
              type="text"
              value={playerName}
              onChange={(e) => setPlayerName(e.target.value)}
              placeholder="선수 이름 입력"
              className="w-full border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
              disabled={!isConnected || loading}
            />
            <button
              onClick={handleGetPlayerStats}
              disabled={!isConnected || !playerName.trim() || loading}
              className="w-full bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700 disabled:bg-gray-400 transition-colors"
            >
              {loading && pendingReqId?.includes('getPlayerStats') ? <LoadingSpinner /> : '선수 정보 보기'}
            </button>
          </div>

          <div className="text-xs text-gray-500 bg-gray-50 p-3 rounded">
            <div className="font-medium mb-1">💡 사용 팁:</div>
            <ul className="space-y-1">
              <li>• "팀 순위 알려줘" 같은 자연어로 질문 가능</li>
              <li>• "오늘 경기 일정" 또는 "2024-08-15 경기"</li>
              <li>• "이승현 선수 정보" 같은 선수 조회</li>
              <li>• 상위 3팀은 노란색으로 강조됩니다</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Chatbot 