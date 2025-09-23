import { useEffect, useRef, useState, useMemo } from 'react'

// 백엔드 베이스 URL (환경변수 우선)
const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const mcpBase = import.meta.env.VITE_MCP_BASE_URL || 'ws://localhost:8080/mcp'
const resolveLogoUrl = (url) => {
  if (!url) return url
  if (url.startsWith('/')) return `${apiBase}${url}`
  return url
}

function FloatingChatbot() {
  const [isOpen, setIsOpen] = useState(false)
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [isConnected, setIsConnected] = useState(false)
  const [pendingReqId, setPendingReqId] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [position, setPosition] = useState({ x: 20, y: 20 })

  // 페이지네이션 상태
  const [currentPage, setCurrentPage] = useState(1)
  const [itemsPerPage] = useState(5)

  const wsRef = useRef(null)
  const endRef = useRef(null)
  const chatbotRef = useRef(null)
  const buttonRef = useRef(null)



  // 에러 토스트 자동 제거
  useEffect(() => {
    if (error) {
      const timer = setTimeout(() => setError(null), 5000)
      return () => clearTimeout(timer)
    }
  }, [error])

  // 드래그 기능 (요청에 따라 비활성화)
  const [isDragging, setIsDragging] = useState(false)
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 })

  const handleMouseDown = (e) => {
    e.preventDefault()
    setIsDragging(true)
    const clientX = e.clientX || e.touches?.[0]?.clientX || 0
    const clientY = e.clientY || e.touches?.[0]?.clientY || 0
    const viewportWidth = window.innerWidth || document.documentElement.clientWidth || 0
    const viewportHeight = window.innerHeight || document.documentElement.clientHeight || 0
    setDragStart({
      x: (viewportWidth - clientX) - position.x,
      y: (viewportHeight - clientY) - position.y
    })
  }

  const handleMouseMove = (e) => {
    if (isDragging) {
      const clientX = e.clientX || e.touches?.[0]?.clientX || 0
      const clientY = e.clientY || e.touches?.[0]?.clientY || 0
      const viewportWidth = window.innerWidth || document.documentElement.clientWidth || 0
      const viewportHeight = window.innerHeight || document.documentElement.clientHeight || 0
      setPosition({
        x: (viewportWidth - clientX) - dragStart.x,
        y: (viewportHeight - clientY) - dragStart.y
      })
    }
  }

  const handleMouseUp = () => {
    setIsDragging(false)
  }

  useEffect(() => {
    if (isDragging) {
      document.addEventListener('mousemove', handleMouseMove)
      document.addEventListener('mouseup', handleMouseUp)
      document.addEventListener('touchmove', handleMouseMove, { passive: false })
      document.addEventListener('touchend', handleMouseUp)
      return () => {
        document.removeEventListener('mousemove', handleMouseMove)
        document.removeEventListener('mouseup', handleMouseUp)
        document.removeEventListener('touchmove', handleMouseMove)
        document.removeEventListener('touchend', handleMouseUp)
      }
    }
  }, [isDragging, dragStart])

  // WebSocket 연결
  useEffect(() => {
    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      return
    }

    const connectWebSocket = () => {
      const ws = new WebSocket(mcpBase)

      ws.onopen = () => {
        setIsConnected(true)
        appendSystem('안녕하세요! KBO 야구 정보 AI 어시스턴트입니다.')
        const reqId = `list-${Date.now()}`
        setPendingReqId(reqId)
        ws.send(JSON.stringify({ type: 'tools/list', requestId: reqId }))
      }

      ws.onmessage = (e) => {
        try {
          const data = JSON.parse(e.data)
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
        setIsConnected(false)
        setLoading(false)

        if (event.code !== 1000) {
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

  // 고유 ID 생성
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



  // 입력 처리 개선 - 더 스마트한 인식
  const sendFreeform = () => {
    if (!input.trim() || !isConnected) return

    const userInput = input.trim()
    appendUser(userInput)
    setInput('')

    // 페이지네이션 리셋
    resetPagination()

    // 입력 내용을 분석해서 적절한 도구 호출
    if (isDateInput(userInput)) {
      // 날짜 형식이면 경기 일정 조회
      const formattedDate = formatDateInput(userInput)
      callTool('getGameSchedule', { date: formattedDate })
    } else if (isPlayerNameInput(userInput)) {
      // 선수 이름이면 선수 조회
      callTool('getPlayerStats', { playerName: userInput })
    } else {
      // 그 외는 일반 메시지로 처리
      appendSystem('죄송합니다. 명확한 요청을 이해하지 못했습니다. 날짜(예: 2024-09-03, 9월 3일) 또는 선수 이름을 입력해주세요.')
    }
  }

  // 날짜 입력 판별 함수
  const isDateInput = (input) => {
    // YYYY-MM-DD 형식
    if (/^\d{4}-\d{2}-\d{2}$/.test(input)) return true

    // YYYYMMDD 형식 (20250903)
    if (/^\d{8}$/.test(input)) return true

    // 한국어 날짜 형식 (8월 7일, 9월 3일)
    if (/^\d{1,2}월\s*\d{1,2}일$/.test(input)) return true

    // 오늘, 어제, 내일, 모레 관련 표현들
    if (/^(오늘|어제|내일|모레)(경기|경기\s*일정)?$/.test(input)) return true

    return false
  }

  // 날짜 입력을 표준 형식으로 변환
  const formatDateInput = (input) => {
    // YYYY-MM-DD 형식은 그대로 반환
    if (/^\d{4}-\d{2}-\d{2}$/.test(input)) return input

    // YYYYMMDD 형식을 YYYY-MM-DD로 변환
    if (/^\d{8}$/.test(input)) {
      const year = input.substring(0, 4)
      const month = input.substring(4, 6)
      const day = input.substring(6, 8)
      return `${year}-${month}-${day}`
    }

    // 한국어 날짜 형식을 YYYY-MM-DD로 변환
    if (/^\d{1,2}월\s*\d{1,2}일$/.test(input)) {
      const currentYear = new Date().getFullYear()
      const monthMatch = input.match(/(\d{1,2})월/)
      const dayMatch = input.match(/(\d{1,2})일/)

      if (monthMatch && dayMatch) {
        const month = monthMatch[1].padStart(2, '0')
        const day = dayMatch[1].padStart(2, '0')
        return `${currentYear}-${month}-${day}`
      }
    }

    // 오늘, 어제, 내일, 모레 처리
    const today = new Date()
    switch (input) {
      case '오늘':
      case '오늘경기':
      case '오늘경기 일정':
        return today.toISOString().split('T')[0]
      case '어제':
      case '어제경기':
      case '어제경기 일정':
        const yesterday = new Date(today)
        yesterday.setDate(today.getDate() - 1)
        return yesterday.toISOString().split('T')[0]
      case '내일':
      case '내일경기':
      case '내일경기 일정':
        const tomorrow = new Date(today)
        tomorrow.setDate(today.getDate() + 1)
        return tomorrow.toISOString().split('T')[0]
      case '모레':
      case '모레경기':
      case '모레경기 일정':
        const dayAfterTomorrow = new Date(today)
        dayAfterTomorrow.setDate(today.getDate() + 2)
        return dayAfterTomorrow.toISOString().split('T')[0]
      default:
        return input
    }
  }

  // 선수 이름 입력 판별 함수 - 개선된 버전
  const isPlayerNameInput = (input) => {
    // 1글자 이상의 한글/숫자만 허용 (영어 제외)
    if (input.length >= 1) {
      // 한글, 숫자만 허용 (영어, 공백 제외)
      const validPattern = /^[가-힣0-9]+$/
      return validPattern.test(input)
    }
    return false
  }

  // 페이지네이션 함수들
  const goToPage = (page) => {
    setCurrentPage(page)
  }

  const nextPage = () => {
    setCurrentPage(prev => prev + 1)
  }

  const prevPage = () => {
    setCurrentPage(prev => Math.max(prev - 1, 1))
  }

  const resetPagination = () => {
    setCurrentPage(1)
  }

  // MCP 도구 호출
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

  // 핸들러들 - 개선된 버전
  const handleGetTeamRanking = () => {
    // 팀 순위는 바로 조회
    callTool('getTeamRanking', {})
  }

  const handleGetGameSchedule = () => {
    // AI가 날짜 입력을 요청
    appendSystem('경기 일정을 조회하시려면 원하시는 경기 날짜를 입력해주세요. (예: 2024-09-03, 9월 3일)')
    setInput('')
    // 입력창에 포커스
    setTimeout(() => {
      const inputElement = document.querySelector('input[placeholder*="야구 관련 질문"]')
      if (inputElement) inputElement.focus()
    }, 100)
  }

  const handleGetPlayerStats = () => {
    // AI가 선수 이름 입력을 요청
    appendSystem('선수 정보를 조회하시려면 조회를 원하시는 선수 이름을 입력해주세요. (예: 김서현)')
    setInput('')
    // 입력창에 포커스
    setTimeout(() => {
      const inputElement = document.querySelector('input[placeholder*="야구 관련 질문"]')
      if (inputElement) inputElement.focus()
    }, 100)
  }

  // 로딩 스피너
  const LoadingSpinner = () => (
    <div className="inline-flex items-center">
      <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
      </svg>
      처리중...
    </div>
  )

  // 에러 토스트
  const ErrorToast = () => {
    if (!error) return null
    return (
      <div className="absolute top-4 right-4 bg-red-500 text-white px-4 py-2 rounded-lg shadow-lg z-50 max-w-xs text-sm">
        <div className="flex items-center">
          <svg className="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
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

    // useMemo로 이미지 URL 메모이제이션 - ranks가 변경될 때만 재계산
    const memoizedRanks = useMemo(() => {
      return ranks.map(rank => ({
        ...rank,
        logoUrl: rank.teamLogoUrl ? resolveLogoUrl(rank.teamLogoUrl) : null
      }))
    }, [ranks])

    return (
      <div className="space-y-2">
        <div className="font-semibold text-gray-800">현재 시즌 팀 순위</div>
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
              {memoizedRanks.map((r, idx) => (
                <tr key={idx} className={`border-t ${idx < 3 ? 'bg-yellow-50 font-medium' : ''}`}>
                  <td className="py-1 pr-3">{r.rank}</td>
                  <td className="py-1 pr-3 flex items-center gap-2">
                    {r.logoUrl ? <img src={r.logoUrl} alt={r.teamName} className="w-6 h-6" /> : null}
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
          <div className="font-semibold text-gray-800">경기 일정</div>
          <div className="text-sm text-gray-600">
            {date ? `${date} 경기 일정이 없습니다.` : '오늘 경기 일정이 없습니다.'}
          </div>
        </div>
      )
    }
    return (
      <div className="space-y-2">
        <div className="font-semibold text-gray-800">경기 일정</div>
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
                  {s.homeStarterName && <span>{s.homeStarterName}</span>}
                  {s.awayStarterName && <span>{s.awayStarterName}</span>}
                </div>
                <div className="flex items-center gap-2">
                  {s.stadium && <span>{s.stadium}</span>}
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
                      {s.statusCode === 'RESULT' ? '경기종료' :
                        s.statusCode === 'BEFORE' && s.statusInfo === '경기취소' ? '경기취소' :
                          s.statusCode === 'LIVE' ? '경기중' :
                            s.statusInfo || s.statusCode}
                    </span>

                    {s.statusCode === 'LIVE' && s.statusInfo && (
                      <span className="text-gray-600 bg-gray-100 px-2 py-1 rounded">
                        {s.statusInfo}
                      </span>
                    )}
                  </div>
                  {s.broadChannel && <span>{s.broadChannel}</span>}
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
    if (content.candidates) {
      const totalCandidates = content.candidates.length
      const totalPages = Math.ceil(totalCandidates / itemsPerPage)
      const startIndex = (currentPage - 1) * itemsPerPage
      const endIndex = startIndex + itemsPerPage
      const currentCandidates = content.candidates.slice(startIndex, endIndex)

      return (
        <div className="space-y-2">
          <div className="font-semibold text-gray-800">선수 후보 ({totalCandidates}명)</div>
          <div className="text-sm text-gray-600 mb-2">{content.message}</div>

          {/* 선수 목록 */}
          <div className="space-y-1">
            {currentCandidates.map((c, idx) => (
              <button
                key={c.playerId || idx}
                onClick={() => callTool('getPlayerStats', { playerId: c.playerId })}
                className="w-full text-left p-2 border rounded hover:bg-blue-50 transition-colors"
              >
                <div className="font-medium">{c.playerName}</div>
                <div className="text-xs text-gray-500">{c.teamName} • {c.position}</div>
              </button>
            ))}
          </div>

          {/* 페이지네이션 */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between mt-3 pt-2 border-t border-gray-200">
              <button
                onClick={prevPage}
                disabled={currentPage === 1}
                className="px-3 py-1 text-xs bg-gray-200 text-gray-700 rounded hover:bg-gray-300 disabled:bg-gray-100 disabled:text-gray-400"
              >
                이전
              </button>

              <div className="flex items-center gap-1">
                {Array.from({ length: totalPages }, (_, i) => i + 1).map(page => (
                  <button
                    key={page}
                    onClick={() => goToPage(page)}
                    className={`px-2 py-1 text-xs rounded ${currentPage === page
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                      }`}
                  >
                    {page}
                  </button>
                ))}
              </div>

              <button
                onClick={nextPage}
                disabled={currentPage === totalPages}
                className="px-3 py-1 text-xs bg-gray-200 text-gray-700 rounded hover:bg-gray-300 disabled:bg-gray-100 disabled:text-gray-400"
              >
                다음
              </button>
            </div>
          )}
        </div>
      )
    }

    const stats = content?.stats
    if (!stats) {
      return (
        <div className="space-y-2">
          <div className="font-semibold text-gray-800">선수 정보</div>
          <div className="text-sm text-gray-600">{content.message || '선수 정보를 찾을 수 없습니다.'}</div>
        </div>
      )
    }

    // 스탯 타입에 따른 한국어 표시
    const getPositionKorean = (type) => {
      switch (type) {
        case 'Batter': return '타자'
        case 'Pitcher': return '투수'
        default: return type
      }
    }

    // 스탯 이름을 한국어로 변환
    const getStatKorean = (key) => {
      const statMap = {
        // 투수 스탯
        'games': '경기수',
        'wins': '승',
        'losses': '패',
        'strikeouts': '탈삼진',
        'era': '방어율',
        'saves': '세이브',
        'holds': '홀드',
        'whip': 'WHIP',
        'inningsPitched': '이닝',
        'hits': '피안타',
        'runs': '실점',
        'earnedRuns': '자책점',
        'walks': '볼넷',
        'homeRuns': '피홈런',
        'battersFaced': '타자수',
        'wildPitches': '폭투',
        'balks': '보크',
        'strikeoutsPer9': '9이닝당탈삼진',
        'walksPer9': '9이닝당볼넷',
        'hitsPer9': '9이닝당피안타',
        'homeRunsPer9': '9이닝당피홈런',

        // 타자 스탯
        'sluggingPercentage': '장타율',
        'battingAverage': '타율',
        'homeRuns': '홈런',
        'runsBattedIn': '타점',
        'onBasePercentage': '출루율',
        'ops': 'OPS',
        'atBats': '타수',
        'hits': '안타',
        'doubles': '2루타',
        'triples': '3루타',
        'runs': '득점',
        'stolenBases': '도루',
        'caughtStealing': '도루실패',
        'sacrificeFlies': '희생플라이',
        'sacrificeBunts': '희생번트',
        'intentionalWalks': '고의4구',
        'hitByPitch': '사구',
        'groundIntoDoublePlays': '병살타',
        'plateAppearances': '타석',
        'totalBases': '총루타',
        'extraBaseHits': '장타',
        'runsCreated': '득점창출',
        'isolatedPower': '순수장타율',
        'battingAverageOnBallsInPlay': '인플레이타율'
      }
      return statMap[key] || key
    }

    return (
      <div className="space-y-3">
        <div className="border-b border-gray-200 pb-2">
          <div className="font-semibold text-lg text-gray-800">
            {content.playerName} ({content.teamName})
          </div>
          <div className="text-sm text-gray-600">{getPositionKorean(stats.type)}</div>
        </div>

        <div className="space-y-1">
          {Object.entries(stats)
            .filter(([k]) => k !== 'type')
            .sort(([a], [b]) => {
              // 스탯 우선순위 정의
              const priorityOrder = {
                // 투수 주요 스탯 순서
                'games': 1,
                'wins': 2,
                'losses': 3,
                'strikeouts': 4,
                'saves': 5,
                'holds': 6,
                'era': 7,
                'whip': 8,
                'inningsPitched': 9,
                'hits': 10,
                'runs': 11,
                'earnedRuns': 12,
                'walks': 13,
                'homeRuns': 14,
                'battersFaced': 15,
                'wildPitches': 16,
                'balks': 17,
                'strikeoutsPer9': 18,
                'walksPer9': 19,
                'hitsPer9': 20,
                'homeRunsPer9': 21,

                // 타자 주요 스탯 순서
                'battingAverage': 22,
                'homeRuns': 23,
                'onBasePercentage': 24,
                'sluggingPercentage': 25,
                'ops': 26,
                'atBats': 27,
                'hits': 28,
                'runsBattedIn': 29,
                'doubles': 30,
                'triples': 31,
                'runs': 32,
                'stolenBases': 33,
                'caughtStealing': 34,
                'sacrificeFlies': 35,
                'sacrificeBunts': 36,
                'intentionalWalks': 37,
                'hitByPitch': 38,
                'groundIntoDoublePlays': 39,
                'plateAppearances': 40,
                'totalBases': 41,
                'extraBaseHits': 42,
                'runsCreated': 43,
                'isolatedPower': 44,
                'battingAverageOnBallsInPlay': 45
              }

              const aPriority = priorityOrder[a] || 999
              const bPriority = priorityOrder[b] || 999
              return aPriority - bPriority
            })
            .map(([k, v]) => (
              <div key={k} className="flex justify-between items-center py-2 px-3 bg-white border-b border-gray-100 hover:bg-gray-50 transition-colors">
                <span className="text-sm font-medium text-gray-700">{getStatKorean(k)}</span>
                <span className="text-sm font-semibold text-gray-900">{String(v)}</span>
              </div>
            ))}
        </div>

        {/* 선수 정보 바로가기 링크 - 지표 아래쪽으로 이동 */}
        {content.playerId && (
          <div className="mt-4 pt-3 border-t border-gray-200">
            <a
              href={`${apiBase}/player/${content.playerId}`}
              target="_blank"
              rel="noopener noreferrer"
              className="block border border-gray-200 rounded-lg px-3 py-2 bg-white hover:bg-gray-50 transition-colors"
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-gray-800 text-sm font-medium">
                  <svg className="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                  </svg>
                  선수 정보 바로가기
                </div>
                <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </div>
            </a>
          </div>
        )}
      </div>
    )
  }

  const renderPayloadPretty = (payload) => {
    const c = payload?.content ?? payload
    if (!c || typeof c !== 'object') return null

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
    <div
      className="fixed z-50"
      style={{
        right: `${position.x}px`,
        bottom: `${position.y}px`,
        transform: 'translate(0, 0)'
      }}
    >
      {/* 플로팅 챗봇 버튼 */}
      <button
        ref={buttonRef}
        onClick={() => setIsOpen(!isOpen)}
        className={`w-16 h-16 bg-orange-500 hover:bg-orange-600 text-white rounded-full shadow-lg transition-all duration-300 flex items-center justify-center ${isOpen ? 'scale-110' : 'scale-100'}`}
        // 드래그 비활성화
        onMouseDown={handleMouseDown}
        onTouchStart={handleMouseDown}
        style={{ userSelect: 'none', cursor: 'pointer' }}
      >
        {isOpen ? (
          <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        ) : (
          <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
        )}

        {/* 연결 상태 표시 */}
        <div className={`absolute -top-1 -right-1 w-4 h-4 rounded-full border-2 border-white ${isConnected ? 'bg-green-500' : 'bg-red-500'
          }`} />
      </button>

      {/* 챗봇 패널 */}
      {isOpen && (
        <div
          ref={chatbotRef}
          className="absolute bottom-20 right-0 w-80 sm:w-96 h-[600px] sm:h-[700px] bg-white rounded-lg shadow-2xl border flex flex-col"
        >
          <ErrorToast />

          {/* 헤더 - 고정 (flex-shrink-0) */}
          <div className="bg-orange-600 text-white p-4 flex items-center justify-between flex-shrink-0">
            <div className="font-semibold">KBO AI 어시스턴트</div>
            <div className={`text-xs ${isConnected ? 'text-green-200' : 'text-red-200'}`}>
              {isConnected ? '🟢 연결됨' : '🔴 연결 안 됨'}
            </div>
          </div>

          {/* 채팅 영역 - 스크롤 가능 (flex-1) */}
          <div className="flex-1 p-4 overflow-y-auto space-y-3 bg-gray-50 min-h-0">
            {messages.map((m) => (
              <div key={m.id}>{renderMessage(m)}</div>
            ))}
            <div ref={endRef} />
          </div>

          {/* 입력 영역 - 고정 (flex-shrink-0) */}
          <div className="p-4 border-t bg-white flex-shrink-0">
            <div className="flex space-x-2">
              <input
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') sendFreeform() }}
                placeholder="야구 관련 질문을 입력하세요..."
                className="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500"
                disabled={!isConnected || loading}
              />
              <button
                onClick={sendFreeform}
                disabled={!isConnected || !input.trim() || loading}
                className="bg-orange-600 text-white px-3 py-2 rounded-lg hover:bg-orange-700 transition-colors disabled:bg-gray-400 text-sm"
              >
                {loading ? <LoadingSpinner /> : '전송'}
              </button>
            </div>
          </div>

          {/* 도구 패널 - 간소화 (팀 순위만) */}
          <div className="p-4 border-t bg-gray-50 space-y-3 flex-shrink-0">
            <div className="flex justify-center">
              <button
                onClick={handleGetTeamRanking}
                disabled={!isConnected || loading}
                className="bg-green-600 text-white px-6 py-2 rounded text-sm hover:bg-green-700 disabled:bg-gray-400 transition-colors"
              >
                팀 순위 조회
              </button>
            </div>

            <div className="text-xs text-gray-500 text-center">
              날짜나 선수 이름을 입력창에 직접 입력해보세요!
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default FloatingChatbot
