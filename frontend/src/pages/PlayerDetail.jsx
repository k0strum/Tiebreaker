import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "../utils/axios";
import { Line } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";

// Chart.js 등록
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

function PlayerDetail() {
  const { playerId } = useParams();
  const navigate = useNavigate();
  const [player, setPlayer] = useState(null); // 선수 데이터
  const [loading, setLoading] = useState(true);
  const [isFavorite, setIsFavorite] = useState(false);
  const [selectedBatterMetrics, setSelectedBatterMetrics] = useState([
    "batting",
    "onbase",
    "slugging",
    "ops",
  ]); // 선택된 타자 지표
  const [selectedPitcherMetrics, setSelectedPitcherMetrics] = useState([
    "era",
    "whip",
    "fip",
  ]); // 선택된 투수 지표

  useEffect(() => {
    const fetchPlayerData = async () => {
      setLoading(true);
      try {
        // 백엔드 매핑: GET /api/player/{playerId} (baseURL에 /api 포함됨)
        const { data } = await axios.get(`/player/${playerId}`);
        setPlayer(data);
        setLoading(false);
      } catch (error) {
        console.error("선수 데이터 로딩 실패:", error);
        setPlayer(null);
        setLoading(false);
      }
    };

    fetchPlayerData();
  }, [playerId]);

  const handleBack = () => {
    navigate("/rankings");
  };

  const handleShare = () => {
    if (navigator.share) {
      navigator.share({
        title: `${player?.playerName} 선수 기록`,
        text: `${player?.playerName} 선수의 상세 기록을 확인해보세요!`,
        url: window.location.href,
      });
    } else {
      navigator.clipboard.writeText(window.location.href);
      alert("링크가 클립보드에 복사되었습니다!");
    }
  };

  // 이미지 URL 생성 함수
  const getPlayerImageUrl = (imageUrl) => {
    if (!imageUrl) return '';

    // 파일명만 저장되므로 바로 API 경로와 결합
    return `${window.location.origin}/api/player/images/${imageUrl}`;
  };

  // 이미지 로드 실패 처리 함수
  const handleImageError = (e) => {
    console.log('이미지 로드 실패:', player.imageUrl);
    e.target.style.display = 'none';
    e.target.nextSibling.style.display = 'flex';
  };

  const formatValue = (value, type) => {
    if (value === null || value === undefined) return "-";

    switch (type) {
      case "battingAverage":
      case "onBasePercentage":
      case "sluggingPercentage":
      case "ops":
      case "stolenBasePercentage":
      case "battingAverageWithRunnersInScoringPosition":
      case "pinchHitBattingAverage":
      case "earnedRunAverage":
      case "whip":
      case "battingAverageAgainst":
      case "winningPercentage":
        return value.toFixed(3);
      default:
        return value;
    }
  };

  const formatInnings = (integer, fraction) => {
    if (integer === null || integer === undefined) return "-";
    if (fraction === null || fraction === undefined) return `${integer}.0`;
    return `${integer}.${fraction}`;
  };

  // 리그 상수 확인용, 현재 사용 안함
  // const formatNum = (value, digits = 3) => {
  //   if (value === null || value === undefined || Number.isNaN(Number(value)))
  //     return "-";
  //   return Number(value).toFixed(digits);
  // };

  // 선수 스탯 계산
  const kboConstants = player?.kboConstants || {};
  const cFipConst = kboConstants.cFip ?? 3.2;

  // 타자 스탯
  // 타율
  const calculateBattingAverage = (stats) => {
    return stats.atBats > 0 ? stats.hits / stats.atBats : 0;
  };

  // 출루율 (kbo에서 월간 희생플라이 제공을 안하므로 타석수로 계산)
  const calculateOnBasePercentage = (stats) => {
    const pa = stats.plateAppearances || 0;
    if (pa <= 0) return 0;
    return (
      ((stats.hits || 0) + (stats.walks || 0) + (stats.hitByPitch || 0)) / pa
    );
  };

  // 장타율
  const calculateSluggingPercentage = (stats) => {
    const ab = stats.atBats || 0;
    if (ab <= 0) return 0;
    const singles =
      (stats.hits || 0) -
      (stats.homeRuns || 0) -
      (stats.doubles || 0) -
      (stats.triples || 0);
    return (
      (singles +
        2 * (stats.doubles || 0) +
        3 * (stats.triples || 0) +
        4 * (stats.homeRuns || 0)) /
      ab
    );
  };

  // ops
  const calculateOPS = (stats) => {
    return (
      calculateOnBasePercentage(stats) + calculateSluggingPercentage(stats)
    );
  };

  // 투수 스탯
  // 이닝 계산
  const inningsCalculate = (Integer, Fraction) => {
    const i = Integer || 0;
    const f = Fraction || 0;
    return i + f / 3;
  };
  // 방어율
  const calculateERA = (stats) => {
    const totalInnings = inningsCalculate(
      stats.inningsPitchedInteger,
      stats.inningsPitchedFraction
    );
    return totalInnings > 0 ? (stats.earnedRuns * 9) / totalInnings : 0;
  };

  // WHIP
  const calculateWHIP = (stats) => {
    const totalInnings = inningsCalculate(
      stats.inningsPitchedInteger,
      stats.inningsPitchedFraction
    );
    return totalInnings > 0 ? (stats.hitsAllowed + stats.walksAllowed) / totalInnings : 0;
  };

  // FIP
  const calculateFIP = (stats) => {
    const ip = inningsCalculate(
      stats.inningsPitchedInteger,
      stats.inningsPitchedFraction
    );
    if (ip <= 0) return 0;
    const hr = stats.homeRunsAllowed ?? stats.homeRuns; // 월별/시즌 필드 호환
    const bb = stats.walksAllowed ?? stats.walks;
    const hbp = stats.hitByPitch || 0;
    const so = stats.strikeouts || 0;
    const fipHead = -2 * so + 3 * ((bb || 0) + hbp) + 13 * (hr || 0);
    return fipHead / ip + cFipConst;
  };

  if (loading) {
    return (
      <div className="container mx-auto p-8">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 rounded mb-6"></div>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="h-64 bg-gray-200 rounded"></div>
            <div className="lg:col-span-2 space-y-4">
              <div className="h-12 bg-gray-200 rounded"></div>
              <div className="h-32 bg-gray-200 rounded"></div>
              <div className="h-32 bg-gray-200 rounded"></div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (!player) {
    return (
      <div className="container mx-auto p-8 text-center">
        <div className="text-red-500 text-xl mb-4">⚠️</div>
        <h2 className="text-xl font-semibold text-gray-800 mb-2">
          선수를 찾을 수 없습니다
        </h2>
        <p className="text-gray-600 mb-4">
          요청하신 선수의 정보가 존재하지 않습니다.
        </p>
        <button
          onClick={handleBack}
          className="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600"
        >
          목록으로 돌아가기
        </button>
      </div>
    );
  }

  const hasAnyStats = player.batterStats || player.pitcherStats;

  return (
    <div className="container mx-auto p-8">
      {/* 헤더 */}
      <div className="flex items-center justify-between mb-6">
        <button
          onClick={handleBack}
          className="flex items-center space-x-2 text-blue-600 hover:text-blue-800 font-medium"
        >
          <span>←</span>
          <span>뒤로가기</span>
        </button>
        <div className="flex items-center space-x-3">
          <button
            onClick={handleShare}
            className="text-gray-600 hover:text-gray-800 text-lg"
          >
            📤
          </button>
        </div>
      </div>

      {/* 프로필 섹션 */}
      <div className="bg-white rounded-lg shadow-md p-6 mb-6">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* 프로필 이미지 */}
          <div className="flex justify-center lg:justify-start">
            <div className="relative">
              <div className="w-32 h-32 bg-gray-300 rounded-full flex items-center justify-center overflow-hidden">
                {player.imageUrl ? (
                  <img
                    src={getPlayerImageUrl(player.imageUrl)}
                    alt={`${player.playerName} 프로필`}
                    className="w-full h-full object-cover"
                    onError={handleImageError}
                  />
                ) : null}
                <span
                  className="text-4xl"
                  style={{ display: player.imageUrl ? "none" : "flex" }}
                >
                  👤
                </span>
              </div>
              <div className="absolute -bottom-2 -right-2 w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
                <span className="text-white text-sm font-bold">
                  {player.backNumber}
                </span>
              </div>
            </div>
          </div>

          {/* 프로필 정보 */}
          <div className="lg:col-span-2">
            <h1 className="text-3xl font-bold text-gray-800 mb-2">
              {player.playerName}
            </h1>
            <br />
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <div className="flex items-center space-x-2">
                  <span className="text-gray-600">팀:</span>
                  <span className="font-semibold text-blue-600">
                    {player.teamName}
                  </span>
                </div>
                <div className="flex items-center space-x-2">
                  <span className="text-gray-600">포지션:</span>
                  <span className="font-semibold">{player.position}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <span className="text-gray-600">등번호:</span>
                  <span className="font-semibold">#{player.backNumber}</span>
                </div>
              </div>
              <div className="space-y-2">
                <div className="flex items-center space-x-2">
                  <span className="text-gray-600">생년월일:</span>
                  <span className="font-semibold">{player.birthday}</span>
                </div>
                {player.heightWeight && (
                  <div className="flex items-center space-x-2">
                    <span className="text-gray-600">신체:</span>
                    <span className="font-semibold">{player.heightWeight}</span>
                  </div>
                )}
                {player.draftRank && (
                  <div className="flex items-center space-x-2">
                    <span className="text-gray-600">지명:</span>
                    <span className="font-semibold">{player.draftRank}</span>
                  </div>
                )}
              </div>
            </div>
            {player.career && (
              <div className="mt-4 p-3 bg-gray-50 rounded-lg">
                <span className="text-gray-600">경력: </span>
                <span className="font-medium">{player.career}</span>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 리그 상수(디버그/확인용)
      {player.kboConstants && (
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 className="text-2xl font-bold text-gray-800 mb-4">리그 상수 (확인용)</h2>
          <div className="overflow-x-auto">
            <table className="min-w-full table-auto">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-2 text-left text-sm font-semibold text-gray-700">연도</th>
                  <th className="px-4 py-2 text-left text-sm font-semibold text-gray-700">wOBA</th>
                  <th className="px-4 py-2 text-left text-sm font-semibold text-gray-700">Scale</th>
                  <th className="px-4 py-2 text-left text-sm font-semibold text-gray-700">eBB</th>
                  <th className="px-4 py-2 text-left text-sm font-semibold text-gray-700">1B</th>
                  <th className="px-4 py-2 text-left text-sm font-semibold text-gray-700">2B</th>
                  <th className="px-4 py-2 text-left text-sm font-semibold text-gray-700">3B</th>
                  <th className="px-4 py-2 text-left text-sm font-semibold text-gray-700">HR</th>
                  <th className="px-4 py-2 text-left text-sm font-semibold text-gray-700">R/ePA</th>
                  <th className="px-4 py-2 text-left text-sm font-semibold text-gray-700">RPW</th>
                  <th className="px-4 py-2 text-left text-sm font-semibold text-gray-700">cFIP</th>
                </tr>
              </thead>
              <tbody>
                <tr className="border-b">
                  <td className="px-4 py-2">{player.kboConstants.year}</td>
                  <td className="px-4 py-2">{formatNum(player.kboConstants.woba)}</td>
                  <td className="px-4 py-2">{formatNum(player.kboConstants.scale)}</td>
                  <td className="px-4 py-2">{formatNum(player.kboConstants.ebb)}</td>
                  <td className="px-4 py-2">{formatNum(player.kboConstants.singles)}</td>
                  <td className="px-4 py-2">{formatNum(player.kboConstants.doubles)}</td>
                  <td className="px-4 py-2">{formatNum(player.kboConstants.triples)}</td>
                  <td className="px-4 py-2">{formatNum(player.kboConstants.homeRuns)}</td>
                  <td className="px-4 py-2">{formatNum(player.kboConstants.runsPerEpa)}</td>
                  <td className="px-4 py-2">{formatNum(player.kboConstants.rpw)}</td>
                  <td className="px-4 py-2">{formatNum(player.kboConstants.cFip, 3)}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      )} */}

      {/* 통합 정보 섹션 */}
      <div className="space-y-6">
        {/* 타자 시즌 기록 */}
        {player.batterStats && (
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-2xl font-bold text-gray-800 mb-6 flex items-center">
              <span className="mr-2">⚾</span>
              {player.batterStats.year} 시즌 타격 성적
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <h3 className="text-lg font-semibold text-gray-700 mb-4">
                  주요 타격 성적
                </h3>
                <div className="space-y-3">
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">타율</span>
                    <span className="font-bold text-blue-600">
                      {formatValue(
                        player.batterStats.battingAverage,
                        "battingAverage"
                      )}
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">홈런</span>
                    <span className="font-bold text-red-600">
                      {player.batterStats.homeRuns}개
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">타점</span>
                    <span className="font-bold text-green-600">
                      {player.batterStats.runsBattedIn}점
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">출루율</span>
                    <span className="font-bold text-purple-600">
                      {formatValue(
                        player.batterStats.onBasePercentage,
                        "onBasePercentage"
                      )}
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">장타율</span>
                    <span className="font-bold text-orange-600">
                      {formatValue(
                        player.batterStats.sluggingPercentage,
                        "sluggingPercentage"
                      )}
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">OPS</span>
                    <span className="font-bold text-indigo-600">
                      {formatValue(player.batterStats.ops, "ops")}
                    </span>
                  </div>
                </div>
              </div>
              <div>
                <h3 className="text-lg font-semibold text-gray-700 mb-4">
                  기타 기록
                </h3>
                <div className="space-y-3">
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">경기수</span>
                    <span className="font-bold">
                      {player.batterStats.games}경기
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">타석</span>
                    <span className="font-bold">
                      {player.batterStats.plateAppearances}타석
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">타수</span>
                    <span className="font-bold">
                      {player.batterStats.atBats}타수
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">안타</span>
                    <span className="font-bold">
                      {player.batterStats.hits}개
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">도루</span>
                    <span className="font-bold text-teal-600">
                      {player.batterStats.stolenBases}개
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">득점</span>
                    <span className="font-bold">
                      {player.batterStats.runs}점
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* 타자 월별 기록 */}
        {player.batterMonthlyStats && player.batterMonthlyStats.length > 0 && (
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-2xl font-bold text-gray-800 mb-6 flex items-center">
              <span className="mr-2">📈</span>
              월별 타격 성적 변화
            </h2>

            {/* 타자 차트 선택 */}
            <div className="mb-6">
              <h3 className="text-lg font-semibold text-gray-700 mb-4">
                차트 지표 선택
              </h3>
              <div className="flex flex-wrap gap-4 p-4 bg-gray-50 rounded-lg">
                <label className="flex items-center space-x-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={selectedBatterMetrics.includes("batting")}
                    onChange={(e) => {
                      if (e.target.checked) {
                        setSelectedBatterMetrics([
                          ...selectedBatterMetrics,
                          "batting",
                        ]);
                      } else {
                        setSelectedBatterMetrics(
                          selectedBatterMetrics.filter((m) => m !== "batting")
                        );
                      }
                    }}
                    className="w-4 h-4 text-blue-600 bg-gray-100 border-gray-300 rounded focus:ring-blue-500"
                  />
                  <span className="text-sm font-medium text-gray-700">
                    타율
                  </span>
                </label>
                <label className="flex items-center space-x-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={selectedBatterMetrics.includes("onbase")}
                    onChange={(e) => {
                      if (e.target.checked) {
                        setSelectedBatterMetrics([
                          ...selectedBatterMetrics,
                          "onbase",
                        ]);
                      } else {
                        setSelectedBatterMetrics(
                          selectedBatterMetrics.filter((m) => m !== "onbase")
                        );
                      }
                    }}
                    className="w-4 h-4 text-green-600 bg-gray-100 border-gray-300 rounded focus:ring-green-500"
                  />
                  <span className="text-sm font-medium text-gray-700">
                    출루율
                  </span>
                </label>
                <label className="flex items-center space-x-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={selectedBatterMetrics.includes("slugging")}
                    onChange={(e) => {
                      if (e.target.checked) {
                        setSelectedBatterMetrics([
                          ...selectedBatterMetrics,
                          "slugging",
                        ]);
                      } else {
                        setSelectedBatterMetrics(
                          selectedBatterMetrics.filter((m) => m !== "slugging")
                        );
                      }
                    }}
                    className="w-4 h-4 text-orange-600 bg-gray-100 border-gray-300 rounded focus:ring-orange-500"
                  />
                  <span className="text-sm font-medium text-gray-700">
                    장타율
                  </span>
                </label>
                <label className="flex items-center space-x-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={selectedBatterMetrics.includes("ops")}
                    onChange={(e) => {
                      if (e.target.checked) {
                        setSelectedBatterMetrics([
                          ...selectedBatterMetrics,
                          "ops",
                        ]);
                      } else {
                        setSelectedBatterMetrics(
                          selectedBatterMetrics.filter((m) => m !== "ops")
                        );
                      }
                    }}
                    className="w-4 h-4 text-indigo-600 bg-gray-100 border-gray-300 rounded focus:ring-indigo-500"
                  />
                  <span className="text-sm font-medium text-gray-700">OPS</span>
                </label>
              </div>
            </div>

            {/* 타자 차트 */}
            {selectedBatterMetrics.length > 0 && (
              <div className="mb-6">
                <h3 className="text-lg font-semibold text-gray-700 mb-4">
                  타자 월간 지표
                </h3>
                <div className="bg-white p-6 rounded-lg border">
                  <div className="h-80">
                    <Line
                      data={{
                        labels: player.batterMonthlyStats.map(
                          (stat) => `${stat.month}월`
                        ),
                        datasets: [
                          ...(selectedBatterMetrics.includes("batting")
                            ? [
                              {
                                label: "타율",
                                data: player.batterMonthlyStats.map((stat) =>
                                  calculateBattingAverage(stat)
                                ),
                                borderColor: "rgb(59, 130, 246)",
                                backgroundColor: "rgba(59, 130, 246, 0.1)",
                                borderWidth: 3,
                                fill: false,
                                tension: 0.4,
                                pointBackgroundColor: "rgb(59, 130, 246)",
                                pointBorderColor: "#fff",
                                pointBorderWidth: 2,
                                pointRadius: 6,
                                pointHoverRadius: 8,
                              },
                            ]
                            : []),
                          ...(selectedBatterMetrics.includes("onbase")
                            ? [
                              {
                                label: "출루율",
                                data: player.batterMonthlyStats.map((stat) =>
                                  calculateOnBasePercentage(stat)
                                ),
                                borderColor: "rgb(16, 185, 129)",
                                backgroundColor: "rgba(16, 185, 129, 0.1)",
                                borderWidth: 3,
                                fill: false,
                                tension: 0.4,
                                pointBackgroundColor: "rgb(16, 185, 129)",
                                pointBorderColor: "#fff",
                                pointBorderWidth: 2,
                                pointRadius: 6,
                                pointHoverRadius: 8,
                              },
                            ]
                            : []),
                          ...(selectedBatterMetrics.includes("slugging")
                            ? [
                              {
                                label: "장타율",
                                data: player.batterMonthlyStats.map((stat) =>
                                  calculateSluggingPercentage(stat)
                                ),
                                borderColor: "rgb(249, 115, 22)",
                                backgroundColor: "rgba(249, 115, 22, 0.1)",
                                borderWidth: 3,
                                fill: false,
                                tension: 0.4,
                                pointBackgroundColor: "rgb(249, 115, 22)",
                                pointBorderColor: "#fff",
                                pointBorderWidth: 2,
                                pointRadius: 6,
                                pointHoverRadius: 8,
                              },
                            ]
                            : []),
                          ...(selectedBatterMetrics.includes("ops")
                            ? [
                              {
                                label: "OPS",
                                data: player.batterMonthlyStats.map((stat) =>
                                  calculateOPS(stat)
                                ),
                                borderColor: "rgb(109, 40, 217)",
                                backgroundColor: "rgba(109, 40, 217, 0.1)",
                                borderWidth: 3,
                                fill: false,
                                tension: 0.4,
                                pointBackgroundColor: "rgb(109, 40, 217)",
                                pointBorderColor: "#fff",
                                pointBorderWidth: 2,
                                pointRadius: 6,
                                pointHoverRadius: 8,
                              },
                            ]
                            : []),
                        ],
                      }}
                      options={{
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                          legend: {
                            position: "top",
                            labels: {
                              usePointStyle: true,
                              padding: 20,
                              font: {
                                size: 14,
                                weight: "bold",
                              },
                            },
                          },
                          tooltip: {
                            mode: "index",
                            intersect: false,
                            backgroundColor: "rgba(0, 0, 0, 0.8)",
                            titleColor: "#fff",
                            bodyColor: "#fff",
                            borderColor: "rgba(255, 255, 255, 0.2)",
                            borderWidth: 1,
                            cornerRadius: 8,
                            displayColors: true,
                            callbacks: {
                              label: function (context) {
                                return `${context.dataset.label
                                  }: ${context.parsed.y.toFixed(3)}`;
                              },
                            },
                          },
                        },
                        scales: {
                          x: {
                            display: true,
                            title: {
                              display: true,
                              text: "월",
                              font: {
                                size: 14,
                                weight: "bold",
                              },
                            },
                            grid: {
                              color: "rgba(0, 0, 0, 0.1)",
                            },
                          },
                          y: {
                            display: true,
                            title: {
                              display: true,
                              text: "비율",
                              font: {
                                size: 14,
                                weight: "bold",
                              },
                            },
                            min: 0,
                            max: function (context) {
                              // 선택된 지표들의 최대값을 계산
                              const allValues = [];

                              if (selectedBatterMetrics.includes("batting")) {
                                allValues.push(
                                  ...player.batterMonthlyStats.map((stat) =>
                                    calculateBattingAverage(stat)
                                  )
                                );
                              }
                              if (selectedBatterMetrics.includes("onbase")) {
                                allValues.push(
                                  ...player.batterMonthlyStats.map((stat) =>
                                    calculateOnBasePercentage(stat)
                                  )
                                );
                              }
                              if (selectedBatterMetrics.includes("slugging")) {
                                allValues.push(
                                  ...player.batterMonthlyStats.map((stat) =>
                                    calculateSluggingPercentage(stat)
                                  )
                                );
                              }
                              if (selectedBatterMetrics.includes("ops")) {
                                allValues.push(
                                  ...player.batterMonthlyStats.map((stat) =>
                                    calculateOPS(stat)
                                  )
                                );
                              }

                              const maxValue = Math.max(...allValues);
                              // OPS가 포함되어 있으면 1.2배, 아니면 1.0배로 설정
                              const multiplier = selectedBatterMetrics.includes(
                                "ops"
                              )
                                ? 1.2
                                : 1.0;
                              return Math.ceil(maxValue * multiplier * 10) / 10;
                            },
                            ticks: {
                              callback: function (value) {
                                return value.toFixed(3);
                              },
                            },
                            grid: {
                              color: "rgba(0, 0, 0, 0.1)",
                            },
                          },
                        },
                        interaction: {
                          mode: "nearest",
                          axis: "x",
                          intersect: false,
                        },
                        elements: {
                          point: {
                            hoverBorderWidth: 3,
                          },
                        },
                      }}
                    />
                  </div>
                </div>
              </div>
            )}
            <div>
              <h3 className="text-lg font-semibold text-gray-700 mb-4">
                월별 상세 기록
              </h3>
              <div className="overflow-x-auto">
                <table className="min-w-full table-auto">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">
                        월
                      </th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">
                        경기
                      </th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">
                        타율
                      </th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">
                        홈런
                      </th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">
                        타점
                      </th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">
                        안타
                      </th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">
                        볼넷
                      </th>
                      <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">
                        삼진
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    {player.batterMonthlyStats.map((stat) => {
                      const battingAverage =
                        stat.atBats > 0 ? stat.hits / stat.atBats : 0;
                      return (
                        <tr
                          key={`${stat.year}-${stat.month}`}
                          className="border-b hover:bg-gray-50"
                        >
                          <td className="px-4 py-3 font-medium">
                            {stat.month}월
                          </td>
                          <td className="px-4 py-3">{stat.games}경기</td>
                          <td className="px-4 py-3">
                            {battingAverage.toFixed(3)}
                          </td>
                          <td className="px-4 py-3">{stat.homeRuns}개</td>
                          <td className="px-4 py-3">{stat.runsBattedIn}점</td>
                          <td className="px-4 py-3">{stat.hits}개</td>
                          <td className="px-4 py-3">{stat.walks}개</td>
                          <td className="px-4 py-3">{stat.strikeouts}개</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
            {/* 정보 패널 */}
            <div className="mt-6 bg-blue-50 rounded-lg p-4">
              <h3 className="font-semibold text-blue-800 mb-2">ℹ️ 지표 기준 안내</h3>
              <div className="text-sm text-blue-700 space-y-1">
                <p>• <strong>타율</strong>: 타자가 안타를 칠 확률</p>
                <p>• <strong>출루율</strong>: 안타를 포함한 출루 확률</p>
                <p>• <strong>장타율</strong>: 타자의 장타력을 나타내는 지표</p>
                <p>• <strong>OPS</strong>: 출루율 + 장타율</p>
              </div>
            </div>
          </div>
        )}

        {/* 투수 시즌 기록 */}
        {player.pitcherStats && (
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-2xl font-bold text-gray-800 mb-6 flex items-center">
              <span className="mr-2">🎯</span>
              {player.pitcherStats.year} 시즌 투구 성적
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <h3 className="text-lg font-semibold text-gray-700 mb-4">
                  주요 투구 성적
                </h3>
                <div className="space-y-3">
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">평균자책점</span>
                    <span className="font-bold text-blue-600">
                      {formatValue(
                        player.pitcherStats.earnedRunAverage,
                        "earnedRunAverage"
                      )}
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">FIP(계산)</span>
                    <span className="font-bold text-rose-600">
                      {formatValue(
                        calculateFIP(player.pitcherStats),
                        "earnedRunAverage"
                      )}
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">승수</span>
                    <span className="font-bold text-green-600">
                      {player.pitcherStats.wins}승
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">패수</span>
                    <span className="font-bold text-red-600">
                      {player.pitcherStats.losses}패
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">세이브</span>
                    <span className="font-bold text-purple-600">
                      {player.pitcherStats.saves}개
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">홀드</span>
                    <span className="font-bold text-orange-600">
                      {player.pitcherStats.holds}개
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">WHIP</span>
                    <span className="font-bold text-indigo-600">
                      {formatValue(player.pitcherStats.whip, "whip")}
                    </span>
                  </div>
                </div>
              </div>
              <div>
                <h3 className="text-lg font-semibold text-gray-700 mb-4">
                  기타 기록
                </h3>
                <div className="space-y-3">
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">경기수</span>
                    <span className="font-bold">
                      {player.pitcherStats.games}경기
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">이닝</span>
                    <span className="font-bold">
                      {formatInnings(
                        player.pitcherStats.inningsPitchedInteger,
                        player.pitcherStats.inningsPitchedFraction
                      )}
                      이닝
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">삼진</span>
                    <span className="font-bold">
                      {player.pitcherStats.strikeouts}개
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">완투</span>
                    <span className="font-bold">
                      {player.pitcherStats.completeGames}개
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">완봉</span>
                    <span className="font-bold">
                      {player.pitcherStats.shutouts}개
                    </span>
                  </div>
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">퀄리티스타트</span>
                    <span className="font-bold">
                      {player.pitcherStats.qualityStarts}개
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* 투수 월별 기록 */}
        {player.pitcherMonthlyStats &&
          player.pitcherMonthlyStats.length > 0 && (
            <div className="bg-white rounded-lg shadow-md p-6">
              <h2 className="text-2xl font-bold text-gray-800 mb-6 flex items-center">
                <span className="mr-2">📊</span>
                월별 투구 성적 변화
              </h2>

              {/* 투수 차트 선택 */}
              <div className="mb-6">
                <h3 className="text-lg font-semibold text-gray-700 mb-4">
                  차트 지표 선택
                </h3>
                <div className="flex flex-wrap gap-4 p-4 bg-gray-50 rounded-lg">
                  <label className="flex items-center space-x-2 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={selectedPitcherMetrics.includes("era")}
                      onChange={(e) => {
                        if (e.target.checked) {
                          setSelectedPitcherMetrics([
                            ...selectedPitcherMetrics,
                            "era",
                          ]);
                        } else {
                          setSelectedPitcherMetrics(
                            selectedPitcherMetrics.filter((m) => m !== "era")
                          );
                        }
                      }}
                      className="w-4 h-4 text-indigo-600 bg-gray-100 border-gray-300 rounded focus:ring-indigo-500"
                    />
                    <span className="text-sm font-medium text-gray-700">
                      평균자책점 (ERA)
                    </span>
                  </label>
                  <label className="flex items-center space-x-2 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={selectedPitcherMetrics.includes("whip")}
                      onChange={(e) => {
                        if (e.target.checked) {
                          setSelectedPitcherMetrics([
                            ...selectedPitcherMetrics,
                            "whip",
                          ]);
                        } else {
                          setSelectedPitcherMetrics(
                            selectedPitcherMetrics.filter((m) => m !== "whip")
                          );
                        }
                      }}
                      className="w-4 h-4 text-red-600 bg-gray-100 border-gray-300 rounded focus:ring-red-500"
                    />
                    <span className="text-sm font-medium text-gray-700">
                      WHIP
                    </span>
                  </label>
                  <label className="flex items-center space-x-2 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={selectedPitcherMetrics.includes("fip")}
                      onChange={(e) => {
                        if (e.target.checked) {
                          setSelectedPitcherMetrics([
                            ...selectedPitcherMetrics,
                            "fip",
                          ]);
                        } else {
                          setSelectedPitcherMetrics(
                            selectedPitcherMetrics.filter((m) => m !== "fip")
                          );
                        }
                      }}
                      className="w-4 h-4 text-purple-600 bg-gray-100 border-gray-300 rounded focus:ring-purple-500"
                    />
                    <span className="text-sm font-medium text-gray-700">
                      FIP
                    </span>
                  </label>
                </div>
              </div>

              {/* 투수 차트 */}
              {selectedPitcherMetrics.length > 0 && (
                <div className="mb-6">
                  <h3 className="text-lg font-semibold text-gray-700 mb-4">
                    투수 월간 지표
                  </h3>
                  <div className="bg-white p-6 rounded-lg border">
                    <div className="h-80">
                      <Line
                        data={{
                          labels: player.pitcherMonthlyStats.map(
                            (stat) => `${stat.month}월`
                          ),
                          datasets: [
                            ...(selectedPitcherMetrics.includes("era")
                              ? [
                                {
                                  label: "평균자책점 (ERA)",
                                  data: player.pitcherMonthlyStats.map(
                                    (stat) => calculateERA(stat)
                                  ),
                                  borderColor: "rgb(239, 68, 68)",
                                  backgroundColor: "rgba(239, 68, 68, 0.1)",
                                  borderWidth: 3,
                                  fill: false,
                                  tension: 0.4,
                                  pointBackgroundColor: "rgb(239, 68, 68)",
                                  pointBorderColor: "#fff",
                                  pointBorderWidth: 2,
                                  pointRadius: 6,
                                  pointHoverRadius: 8,
                                },
                              ]
                              : []),
                            ...(selectedPitcherMetrics.includes("whip")
                              ? [
                                {
                                  label: "WHIP",
                                  data: player.pitcherMonthlyStats.map(
                                    (stat) => calculateWHIP(stat)
                                  ),
                                  borderColor: "rgb(168, 85, 247)",
                                  backgroundColor: "rgba(168, 85, 247, 0.1)",
                                  borderWidth: 3,
                                  fill: false,
                                  tension: 0.4,
                                  pointBackgroundColor: "rgb(168, 85, 247)",
                                  pointBorderColor: "#fff",
                                  pointBorderWidth: 2,
                                  pointRadius: 6,
                                  pointHoverRadius: 8,
                                },
                              ]
                              : []),
                            ...(selectedPitcherMetrics.includes("fip")
                              ? [
                                {
                                  label: "FIP",
                                  data: player.pitcherMonthlyStats.map(
                                    (stat) => calculateFIP(stat)
                                  ),
                                  borderColor: "rgb(239, 68, 68)",
                                  backgroundColor: "rgba(239, 68, 68, 0.1)",
                                  borderWidth: 3,
                                  fill: false,
                                  tension: 0.4,
                                  pointBackgroundColor: "rgb(239, 68, 68)",
                                  pointBorderColor: "#fff",
                                  pointBorderWidth: 2,
                                  pointRadius: 6,
                                  pointHoverRadius: 8,
                                },
                              ]
                              : []),
                          ],
                        }}
                        options={{
                          responsive: true,
                          maintainAspectRatio: false,
                          plugins: {
                            legend: {
                              position: "top",
                              labels: {
                                usePointStyle: true,
                                padding: 20,
                                font: {
                                  size: 14,
                                  weight: "bold",
                                },
                              },
                            },
                            tooltip: {
                              mode: "index",
                              intersect: false,
                              backgroundColor: "rgba(0, 0, 0, 0.8)",
                              titleColor: "#fff",
                              bodyColor: "#fff",
                              borderColor: "rgba(255, 255, 255, 0.2)",
                              borderWidth: 1,
                              cornerRadius: 8,
                              displayColors: true,
                              callbacks: {
                                label: function (context) {
                                  return `${context.dataset.label
                                    }: ${context.parsed.y.toFixed(2)}`;
                                },
                              },
                            },
                          },
                          scales: {
                            x: {
                              display: true,
                              title: {
                                display: true,
                                text: "월",
                                font: {
                                  size: 14,
                                  weight: "bold",
                                },
                              },
                              grid: {
                                color: "rgba(0, 0, 0, 0.1)",
                              },
                            },
                            y: {
                              display: true,
                              title: {
                                display: true,
                                text: "평균자책점",
                                font: {
                                  size: 14,
                                  weight: "bold",
                                },
                              },
                              min: 0,
                              max: function (context) {
                                const maxEra = Math.max(
                                  ...player.pitcherMonthlyStats.map((stat) =>
                                    calculateERA(stat)
                                  )
                                );
                                const maxFip = Math.max(
                                  ...player.pitcherMonthlyStats.map((stat) =>
                                    calculateFIP(stat)
                                  )
                                );
                                return Math.ceil(
                                  Math.max(maxEra, maxFip) * 1.2
                                );
                              },
                              ticks: {
                                callback: function (value) {
                                  return value.toFixed(1);
                                },
                              },
                              grid: {
                                color: "rgba(0, 0, 0, 0.1)",
                              },
                            },
                          },
                          interaction: {
                            mode: "nearest",
                            axis: "x",
                            intersect: false,
                          },
                          elements: {
                            point: {
                              hoverBorderWidth: 3,
                            },
                          },
                        }}
                      />
                    </div>
                  </div>
                </div>
              )}
              <div>
                <h3 className="text-lg font-semibold text-gray-700 mb-4">
                  월별 상세 기록
                </h3>
                <div className="overflow-x-auto">
                  <table className="min-w-full table-auto">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">
                          월
                        </th>
                        <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">
                          경기
                        </th>
                        <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">
                          이닝
                        </th>
                        <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">
                          평균자책점
                        </th>
                        <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">
                          승수
                        </th>
                        <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">
                          패수
                        </th>
                        <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">
                          세이브
                        </th>
                        <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">
                          홀드
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {player.pitcherMonthlyStats.map((stat) => {
                        const era =
                          stat.inningsPitchedInteger > 0
                            ? (stat.earnedRuns * 9) /
                            (stat.inningsPitchedInteger +
                              stat.inningsPitchedFraction / 3)
                            : 0;
                        return (
                          <tr
                            key={`${stat.year}-${stat.month}`}
                            className="border-b hover:bg-gray-50"
                          >
                            <td className="px-4 py-3 font-medium">
                              {stat.month}월
                            </td>
                            <td className="px-4 py-3">{stat.games}경기</td>
                            <td className="px-4 py-3">
                              {formatInnings(
                                stat.inningsPitchedInteger,
                                stat.inningsPitchedFraction
                              )}
                              이닝
                            </td>
                            <td className="px-4 py-3">{era.toFixed(2)}</td>
                            <td className="px-4 py-3">{stat.wins}승</td>
                            <td className="px-4 py-3">{stat.losses}패</td>
                            <td className="px-4 py-3">{stat.saves}개</td>
                            <td className="px-4 py-3">{stat.holds}개</td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </div>
              {/* 정보 패널 */}
              <div className="mt-6 bg-blue-50 rounded-lg p-4">
                <h3 className="font-semibold text-blue-800 mb-2">ℹ️ 지표 기준 안내</h3>
                <div className="text-sm text-blue-700 space-y-1">
                  <p>• <strong>평균자책점</strong>: 9이닝 동안 내주는 자책점의 평균</p>
                  <p>• <strong>WHIP</strong>: 이닝당 타자 출루 횟수</p>
                  <p>• <strong>FIP</strong>: 투수가 제어할 수 없는 자책점을 제외한 평가 지표</p>
                </div>
              </div>
            </div>
          )}

        {/* 기록이 없는 경우 */}
        {!hasAnyStats && (
          <div className="bg-white rounded-lg shadow-md p-6 text-center">
            <div className="text-gray-400 text-6xl mb-4">📊</div>
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              경기 기록이 없습니다
            </h2>
            <p className="text-gray-600 mb-6">
              {player.playerName} 선수의 {new Date().getFullYear()} 시즌 경기
              기록이 없습니다.
            </p>
            <div className="bg-gray-50 rounded-lg p-6 max-w-md mx-auto">
              <h3 className="font-semibold text-gray-700 mb-2">가능한 이유:</h3>
              <ul className="text-sm text-gray-600 space-y-1 text-left">
                <li>• 아직 경기에 출전하지 않음</li>
                <li>• 부상으로 인한 결장</li>
                <li>• 2군에서 활동 중</li>
                <li>• 시즌 중 입단</li>
              </ul>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default PlayerDetail;
