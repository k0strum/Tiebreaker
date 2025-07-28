import { useState } from 'react'
import './App.css'

function App() {
  const [count, setCount] = useState(0)

  return (
    <>
      <div className="app-container">
        <h1>⚾ Tiebreaker</h1>
        <p className="subtitle">KBO 팬 플랫폼</p>
        
        <div className="card">
          <button onClick={() => setCount((count) => count + 1)}>
            카운트: {count}
          </button>
          <p>
            <code>src/App.jsx</code>를 수정하고 저장하면 핫 리로드가 작동합니다
          </p>
        </div>
        
        <div className="features">
          <h2>주요 기능</h2>
          <ul>
            <li>📊 선수 기록실</li>
            <li>💬 실시간 채팅</li>
            <li>🎯 승부 예측</li>
            <li>🤖 AI 정보 챗봇</li>
          </ul>
        </div>
      </div>
    </>
  )
}

export default App
