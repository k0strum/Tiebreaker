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
  const wsRef = useRef(null)
  const endRef = useRef(null)

  // 도구 입력 상태
  const [playerName, setPlayerName] = useState('')
  const [date, setDate] = useState('')

  useEffect(() => {
    const ws = new WebSocket('ws://localhost:8080/mcp')
    ws.onopen = () => {
      setIsConnected(true)
      appendSystem('서버에 연결되었습니다.')
      // 도구 목록 미리 로드
      const reqId = `list-${Date.now()}`
      setPendingReqId(reqId)
      ws.send(JSON.stringify({ type: 'tools/list', requestId: reqId }))
    }
    ws.onmessage = (e) => {
      try {
        const data = JSON.parse(e.data)
        appendServer(data)
      } catch (err) {
        appendServer({ type: 'raw', content: e.data })
      }
    }
    ws.onerror = () => appendSystem('WebSocket 오류가 발생했습니다.')
    ws.onclose = () => setIsConnected(false)
    wsRef.current = ws

    return () => {
      try { ws.close() } catch { }
    }
  }, [])

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const appendSystem = (text) => {
    setMessages((prev) => [...prev, { id: Date.now(), role: 'system', text }])
  }
  const appendUser = (text) => {
    setMessages((prev) => [...prev, { id: Date.now(), role: 'user', text }])
  }
  const appendServer = (payload) => {
    setMessages((prev) => [...prev, { id: Date.now(), role: 'server', payload }])
  }

  const sendFreeform = () => {
    if (!input.trim() || !isConnected) return
    appendUser(input.trim())
    setInput('')
  }

  // MCP 도구 호출 유틸
  const callTool = (toolName, args = {}) => {
    if (!isConnected || !wsRef.current) return
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
    const args = {}
    if (date && date.trim()) args.date = date.trim()
    callTool('getGameSchedule', args)
  }
  const handleGetPlayerStats = () => {
    if (!playerName.trim()) return
    callTool('getPlayerStats', { playerName: playerName.trim() })
  }

  // 렌더링 유틸: 팀 순위
  const TeamRanksView = ({ content }) => {
    const ranks = content?.ranks || []
    if (!Array.isArray(ranks) || ranks.length === 0) {
      return <div className="text-sm text-gray-600">팀 순위 데이터가 없습니다.</div>
    }
    return (
      <div className="space-y-2">
        <div className="font-semibold text-gray-800">팀 순위</div>
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
                <tr key={idx} className="border-t">
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
      </div>
    )
  }

  // 렌더링 유틸: 경기 일정
  const SchedulesView = ({ content }) => {
    const schedules = content?.schedules || []
    const d = content?.date
    if (!Array.isArray(schedules) || schedules.length === 0) {
      return <div className="text-sm text-gray-600">{d ? `${d} 일정이 없습니다.` : '일정 데이터가 없습니다.'}</div>
    }
    return (
      <div className="space-y-2">
        <div className="font-semibold text-gray-800">경기 일정 {d ? `(${d})` : ''}</div>
        <div className="space-y-2">
          {schedules.map((g, idx) => (
            <div key={idx} className="border rounded p-2 text-sm">
              <div className="font-medium text-gray-800">{g.gameId || g.id}</div>
              <div className="text-gray-700">
                <div>시간: {g.gameDateTime || g.gameDate || '-'}</div>
                <div>매치: {(g.homeTeamName || g.homeTeam || '?')} vs {(g.awayTeamName || g.awayTeam || '?')}</div>
                <div>상태: {g.statusCode || g.status || '-'}</div>
              </div>
            </div>
          ))}
        </div>
      </div>
    )
  }

  // 렌더링 유틸: 선수 조회
  const PlayerStatsView = ({ content }) => {
    if (!content) return null
    if (content.candidates) {
      const list = content.candidates
      return (
        <div className="space-y-2">
          <div className="font-semibold text-gray-800">후보가 여러 명입니다. 선택하면 자동으로 재조회합니다.</div>
          <div className="space-y-2">
            {list.map((c, idx) => (
              <button
                key={idx}
                onClick={() => callTool('getPlayerStats', { playerId: c.playerId })}
                className="w-full text-left border rounded p-2 text-sm hover:bg-gray-50 flex items-center justify-between">
                <div>
                  <div className="font-medium">{c.playerName}</div>
                  <div className="text-gray-600">{c.teamName} · {c.position}</div>
                  <div className="text-gray-500">playerId: {c.playerId}</div>
                </div>
                <span className="text-xs text-gray-500">선택</span>
              </button>
            ))}
          </div>
        </div>
      )
    }
    const stats = content.stats
    if (!stats) {
      return (
        <div className="text-sm text-gray-600">{content.message || '선수 정보를 찾을 수 없습니다.'}</div>
      )
    }
    return (
      <div className="space-y-2">
        <div className="font-semibold text-gray-800">{content.playerName} ({content.teamName})</div>
        <div className="text-xs text-gray-500">{stats.type === 'Batter' ? '타자 스탯' : '투수 스탯'}</div>
        <div className="grid grid-cols-2 md:grid-cols-3 gap-2 text-sm">
          {Object.entries(stats).filter(([k]) => k !== 'type').map(([k, v]) => (
            <div key={k} className="border rounded p-2">
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
        <div className="text-center text-gray-500">{msg.text}</div>
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
    <div className="container mx-auto p-8">
      <h1 className="text-3xl font-bold text-orange-600 mb-6">🤖 MCP 챗봇</h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 좌측: 채팅 영역 */}
        <div className="lg:col-span-2 bg-white rounded-lg shadow-md h-[600px] flex flex-col">
          <div className="bg-orange-600 text-white p-4 rounded-t-lg flex items-center justify-between">
            <div className="font-semibold">Tiebreaker MCP Assistant</div>
            <div className={`text-xs ${isConnected ? 'text-green-200' : 'text-red-200'}`}>{isConnected ? '연결됨' : '연결 안 됨'}</div>
          </div>

          <div className="flex-1 p-4 overflow-y-auto space-y-3">
            {messages.map((m) => (
              <div key={m.id}>{renderMessage(m)}</div>
            ))}
            <div ref={endRef} />
          </div>

          <div className="p-4 border-t">
            <div className="flex space-x-2">
              <input
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') sendFreeform() }}
                placeholder="메시지를 입력하세요 (데모: 전송은 로그만 남깁니다)"
                className="flex-1 border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-orange-500"
                disabled={!isConnected}
              />
              <button
                onClick={sendFreeform}
                disabled={!isConnected || !input.trim()}
                className="bg-orange-600 text-white px-6 py-2 rounded-lg hover:bg-orange-700 transition-colors disabled:bg-gray-400"
              >전송</button>
            </div>
          </div>
        </div>

        {/* 우측: 도구 패널 */}
        <div className="bg-white rounded-lg shadow-md p-4 space-y-4">
          <h2 className="font-semibold text-gray-800">도구 실행</h2>

          <div className="space-y-2">
            <div className="text-sm font-medium text-gray-700">팀 순위</div>
            <button
              onClick={handleGetTeamRanking}
              disabled={!isConnected}
              className="w-full bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 disabled:bg-gray-400"
            >getTeamRanking</button>
          </div>

          <div className="space-y-2">
            <div className="text-sm font-medium text-gray-700">경기 일정</div>
            <input
              type="text"
              value={date}
              onChange={(e) => setDate(e.target.value)}
              placeholder="YYYY-MM-DD (비우면 오늘)"
              className="w-full border border-gray-300 rounded px-3 py-2 text-sm"
              disabled={!isConnected}
            />
            <button
              onClick={handleGetGameSchedule}
              disabled={!isConnected}
              className="w-full bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 disabled:bg-gray-400"
            >getGameSchedule</button>
          </div>

          <div className="space-y-2">
            <div className="text-sm font-medium text-gray-700">선수 조회</div>
            <input
              type="text"
              value={playerName}
              onChange={(e) => setPlayerName(e.target.value)}
              placeholder="playerName (필수)"
              className="w-full border border-gray-300 rounded px-3 py-2 text-sm"
              disabled={!isConnected}
            />
            <button
              onClick={handleGetPlayerStats}
              disabled={!isConnected || !playerName.trim()}
              className="w-full bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700 disabled:bg-gray-400"
            >getPlayerStats</button>
          </div>

          <div className="text-xs text-gray-500">
            후보가 여러 명이면 리스트로 표시되며, 항목을 클릭하면 자동 재조회합니다.
          </div>
        </div>
      </div>
    </div>
  )
}

export default Chatbot 