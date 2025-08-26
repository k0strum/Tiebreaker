import React, { useEffect, useState } from 'react';
import { useSSECommentary } from '../utils/useSSECommentary';

export default function CommentaryDemo() {
  const [gameId, setGameId] = useState('DEMO-GAME');
  const { events, connected, error } = useSSECommentary(gameId, '');

  return (
    <div style={{ padding: 16 }}>
      <h2>문자중계 데모</h2>
      <div style={{ marginBottom: 8 }}>
        <input value={gameId} onChange={(e) => setGameId(e.target.value)} placeholder="gameId" />
        <span style={{ marginLeft: 12 }}>
          상태: {connected ? '연결됨' : '해제됨'} {error && `(오류: ${error})`}
        </span>
      </div>
      <ul>
        {events.map((e, idx) => (
          <li key={idx}>
            <span>[{new Date(e.ts).toLocaleTimeString()}] </span>
            <span>{e.text}</span>
            {e.severity === 'HIGHLIGHT' && <strong style={{ color: 'crimson', marginLeft: 6 }}>*</strong>}
          </li>
        ))}
      </ul>
    </div>
  );
}


