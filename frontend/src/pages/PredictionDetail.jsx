import React, { useEffect, useState } from 'react';
import axios from '../utils/axios';

function PredictionDetail() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [stats, setStats] = useState(null);
  const [rankingDaily, setRankingDaily] = useState([]);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const [statsRes, rankingRes] = await Promise.all([
        axios.get('/predictions/stats/my'),
        axios.get('/predictions/ranking/daily'),
      ]);
      if (statsRes.data?.success) setStats(statsRes.data.statistics);
      if (rankingRes.data?.success) setRankingDaily(rankingRes.data.ranking || []);
    } catch (e) {
      setError(e.response?.data?.message || '데이터 로드 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  return (
    <div style={{ maxWidth: 720, margin: '0 auto', padding: 16 }}>
      <h2>내 예측 요약 / 일간 랭킹(테스트)</h2>
      {loading && <div>로딩 중...</div>}
      {error && <div style={{ color: 'red', marginBottom: 8 }}>{error}</div>}

      <section style={{ marginBottom: 16 }}>
        <h3>내 통계</h3>
        {stats ? (
          <ul>
            <li>총 예측 횟수: {stats.totalPredictions}</li>
            <li>총 적중 수: {stats.totalCorrect}</li>
            <li>총 참여 경기 수: {stats.totalGames}</li>
            <li>총 획득 마일리지: {stats.totalMileage}</li>
            <li>적중률: {(stats.accuracy * 100).toFixed(1)}%</li>
          </ul>
        ) : (
          <div>통계가 없습니다.</div>
        )}
      </section>

      <section>
        <h3>일간 랭킹</h3>
        {rankingDaily.length === 0 ? (
          <div>랭킹 데이터가 없습니다.</div>
        ) : (
          <ol>
            {rankingDaily.map((r) => (
              <li key={`${r.memberId}-${r.periodStart}`}>
                {r.rankingPosition}. {r.nickname} — 정답 {r.totalCorrect} / 경기 {r.totalGames} — 마일리지 {r.totalMileage}
              </li>
            ))}
          </ol>
        )}
      </section>
    </div>
  );
}

export default PredictionDetail;


