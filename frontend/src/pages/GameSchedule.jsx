import { useEffect, useState } from 'react'

const API_BASE = ''

export default function GameSchedule() {
  const [date, setDate] = useState(() => new Date().toISOString().slice(0, 10))
  const [games, setGames] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const fetchGames = async (targetDate) => {
    try {
      setLoading(true)
      setError('')
      const res = await fetch(`${API_BASE}/api/games?date=${targetDate}`)
      if (!res.ok) throw new Error('failed to load')
      const data = await res.json()
      setGames(data)
    } catch (e) {
      setError(String(e))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchGames(date)
  }, [])

  return (
    <div style={{ padding: 16 }}>
      <h2>경기 일정</h2>
      <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 12 }}>
        <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        <button onClick={() => fetchGames(date)}>불러오기</button>
        <button onClick={() => { const today = new Date().toISOString().slice(0, 10); setDate(today); fetchGames(today) }}>오늘</button>
      </div>
      {loading && <div>불러오는 중…</div>}
      {error && <div style={{ color: 'red' }}>{error}</div>}
      <ul style={{ listStyle: 'none', padding: 0, display: 'grid', gap: 8 }}>
        {games.map(g => (
          <li key={g.gameId} style={{ border: '1px solid #eee', borderRadius: 8, padding: 12 }}>
            <div style={{ fontSize: 14, color: '#666' }}>{g.gameDate} {g.gameDateTime?.slice(11, 16)} · {g.stadium}</div>
            <div style={{ fontSize: 18, marginTop: 4 }}>
              {g.awayTeamName} {g.awayTeamScore ?? ''} : {g.homeTeamScore ?? ''} {g.homeTeamName}
            </div>
            <div style={{ fontSize: 12, color: '#888', marginTop: 4 }}>상태: {g.statusCode} · 중계: {g.broadChannel || '-'}</div>
          </li>
        ))}
      </ul>
    </div>
  )
}


