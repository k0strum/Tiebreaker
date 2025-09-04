import React, { useEffect, useMemo, useState } from 'react';
import axios from '../utils/axios';
import { useNavigate } from 'react-router-dom';

function Predictions() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [games, setGames] = useState([]);
  const [myPredictions, setMyPredictions] = useState([]);
  const [selected, setSelected] = useState({}); // { [gameId]: 'HOME'|'AWAY' }

  const predictionMap = useMemo(() => {
    const map = {};
    myPredictions.forEach(p => { map[p.gameId] = p; });
    return map;
  }, [myPredictions]);

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      const [gamesRes, myRes] = await Promise.all([
        axios.get('/predictions/today'),
        axios.get('/predictions/my'),
      ]);
      if (gamesRes.data?.success) setGames(gamesRes.data.games || []);
      if (myRes.data?.success) setMyPredictions(myRes.data.predictions || []);
    } catch (e) {
      setError(e.response?.data?.message || '데이터 로드 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadData(); }, []);

  const submitPrediction = async (gameId) => {
    const predictedWinner = selected[gameId];
    if (!predictedWinner) {
      setError('팀을 선택해주세요.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const res = await axios.post('/predictions', { gameId, predictedWinner });
      if (res.data?.success) {
        await loadData();
      } else {
        setError(res.data?.message || '예측 저장에 실패했습니다.');
      }
    } catch (e) {
      setError(e.response?.data?.message || '예측 저장 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 720, margin: '0 auto', padding: 16 }}>
      <h2>오늘의 승부예측</h2>
      {loading && <div>로딩 중...</div>}
      {error && <div style={{ color: 'red', marginBottom: 8 }}>{error}</div>}

      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 12 }}>
        <button onClick={loadData}>새로고침</button>
        <button onClick={() => navigate('/prediction/preview')}>내 예측 상세(테스트)</button>
      </div>

      {games.length === 0 && <div>오늘 예측 가능한 경기가 없습니다.</div>}

      <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
        {games.map(g => {
          const my = predictionMap[g.gameId];
          const current = selected[g.gameId] ?? my?.predictedWinner ?? '';
          return (
            <li key={g.gameId} style={{ border: '1px solid #ddd', borderRadius: 8, padding: 12, marginBottom: 12 }}>
              <div style={{ fontWeight: 600, marginBottom: 8 }}>
                {g.homeTeam} vs {g.awayTeam}
              </div>
              <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
                <label>
                  <input
                    type="radio"
                    name={`pick-${g.gameId}`}
                    value="HOME"
                    checked={current === 'HOME'}
                    onChange={() => setSelected(s => ({ ...s, [g.gameId]: 'HOME' }))}
                  /> HOME({g.homeTeam})
                </label>
                <label>
                  <input
                    type="radio"
                    name={`pick-${g.gameId}`}
                    value="AWAY"
                    checked={current === 'AWAY'}
                    onChange={() => setSelected(s => ({ ...s, [g.gameId]: 'AWAY' }))}
                  /> AWAY({g.awayTeam})
                </label>
                <button onClick={() => submitPrediction(g.gameId)} disabled={loading}>
                  {my ? '수정' : '제출'}
                </button>
                {my && <span style={{ color: '#666' }}>내 예측: {my.predictedWinner}</span>}
              </div>
            </li>
          );
        })}
      </ul>
    </div>
  );
}

export default Predictions;


