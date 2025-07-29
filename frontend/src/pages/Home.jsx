function Home() {
  return (
    <div className="container mx-auto p-8">
      <h1 className="text-3xl font-bold text-indigo-600 mb-6">🧪 더미 페이지</h1>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {/* 카드 1 */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <div className="flex items-center mb-4">
            <div className="w-12 h-12 bg-indigo-500 rounded-full flex items-center justify-center text-white text-xl">
              📊
            </div>
            <div className="ml-4">
              <h3 className="text-lg font-semibold text-gray-800">통계 카드</h3>
              <p className="text-sm text-gray-600">데이터 표시</p>
            </div>
          </div>
          <div className="space-y-2">
            <div className="flex justify-between">
              <span className="text-gray-600">총 사용자</span>
              <span className="font-semibold">1,234명</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">활성 세션</span>
              <span className="font-semibold text-green-600">567개</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">오늘 방문</span>
              <span className="font-semibold text-blue-600">89명</span>
            </div>
          </div>
        </div>

        {/* 카드 2 */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <div className="flex items-center mb-4">
            <div className="w-12 h-12 bg-green-500 rounded-full flex items-center justify-center text-white text-xl">
              ⚡
            </div>
            <div className="ml-4">
              <h3 className="text-lg font-semibold text-gray-800">실시간 상태</h3>
              <p className="text-sm text-gray-600">시스템 모니터링</p>
            </div>
          </div>
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-gray-600">서버 상태</span>
              <span className="px-2 py-1 bg-green-100 text-green-800 rounded-full text-xs">정상</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-gray-600">데이터베이스</span>
              <span className="px-2 py-1 bg-green-100 text-green-800 rounded-full text-xs">연결됨</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-gray-600">API 응답</span>
              <span className="px-2 py-1 bg-yellow-100 text-yellow-800 rounded-full text-xs">지연</span>
            </div>
          </div>
        </div>

        {/* 카드 3 */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <div className="flex items-center mb-4">
            <div className="w-12 h-12 bg-purple-500 rounded-full flex items-center justify-center text-white text-xl">
              🎯
            </div>
            <div className="ml-4">
              <h3 className="text-lg font-semibold text-gray-800">목표 달성</h3>
              <p className="text-sm text-gray-600">진행률 표시</p>
            </div>
          </div>
          <div className="space-y-4">
            <div>
              <div className="flex justify-between text-sm mb-1">
                <span className="text-gray-600">월간 목표</span>
                <span className="font-semibold">75%</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div className="bg-purple-600 h-2 rounded-full" style={{width: '75%'}}></div>
              </div>
            </div>
            <div>
              <div className="flex justify-between text-sm mb-1">
                <span className="text-gray-600">주간 목표</span>
                <span className="font-semibold">90%</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div className="bg-green-600 h-2 rounded-full" style={{width: '90%'}}></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 테이블 섹션 */}
      <div className="mt-8 bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold text-gray-800 mb-4">📋 샘플 데이터 테이블</h2>
        <div className="overflow-x-auto">
          <table className="min-w-full table-auto">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-2 text-left">ID</th>
                <th className="px-4 py-2 text-left">이름</th>
                <th className="px-4 py-2 text-left">상태</th>
                <th className="px-4 py-2 text-left">생성일</th>
                <th className="px-4 py-2 text-left">액션</th>
              </tr>
            </thead>
            <tbody>
              <tr className="border-b hover:bg-gray-50">
                <td className="px-4 py-2">#001</td>
                <td className="px-4 py-2">김철수</td>
                <td className="px-4 py-2">
                  <span className="px-2 py-1 bg-green-100 text-green-800 rounded-full text-xs">활성</span>
                </td>
                <td className="px-4 py-2">2024-06-15</td>
                <td className="px-4 py-2">
                  <button className="text-blue-600 hover:text-blue-800 text-sm">보기</button>
                </td>
              </tr>
              <tr className="border-b hover:bg-gray-50">
                <td className="px-4 py-2">#002</td>
                <td className="px-4 py-2">이영희</td>
                <td className="px-4 py-2">
                  <span className="px-2 py-1 bg-yellow-100 text-yellow-800 rounded-full text-xs">대기</span>
                </td>
                <td className="px-4 py-2">2024-06-14</td>
                <td className="px-4 py-2">
                  <button className="text-blue-600 hover:text-blue-800 text-sm">보기</button>
                </td>
              </tr>
              <tr className="border-b hover:bg-gray-50">
                <td className="px-4 py-2">#003</td>
                <td className="px-4 py-2">박민수</td>
                <td className="px-4 py-2">
                  <span className="px-2 py-1 bg-red-100 text-red-800 rounded-full text-xs">비활성</span>
                </td>
                <td className="px-4 py-2">2024-06-13</td>
                <td className="px-4 py-2">
                  <button className="text-blue-600 hover:text-blue-800 text-sm">보기</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      {/* 버튼 섹션 */}
      <div className="mt-8 bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold text-gray-800 mb-4">🔘 버튼 샘플</h2>
        <div className="flex flex-wrap gap-4">
          <button className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors">
            기본 버튼
          </button>
          <button className="bg-green-600 text-white px-6 py-2 rounded-lg hover:bg-green-700 transition-colors">
            성공 버튼
          </button>
          <button className="bg-red-600 text-white px-6 py-2 rounded-lg hover:bg-red-700 transition-colors">
            위험 버튼
          </button>
          <button className="bg-yellow-600 text-white px-6 py-2 rounded-lg hover:bg-yellow-700 transition-colors">
            경고 버튼
          </button>
          <button className="border border-gray-300 text-gray-700 px-6 py-2 rounded-lg hover:bg-gray-50 transition-colors">
            아웃라인 버튼
          </button>
        </div>
      </div>

      {/* 알림 섹션 */}
      <div className="mt-8 space-y-4">
        <div className="bg-blue-50 border-l-4 border-blue-400 p-4">
          <div className="flex">
            <div className="flex-shrink-0">
              <span className="text-blue-400">ℹ️</span>
            </div>
            <div className="ml-3">
              <p className="text-sm text-blue-700">
                이것은 정보 알림입니다. Tailwind CSS가 정상적으로 작동하고 있습니다.
              </p>
            </div>
          </div>
        </div>

        <div className="bg-green-50 border-l-4 border-green-400 p-4">
          <div className="flex">
            <div className="flex-shrink-0">
              <span className="text-green-400">✅</span>
            </div>
            <div className="ml-3">
              <p className="text-sm text-green-700">
                성공! 모든 컴포넌트가 올바르게 렌더링되었습니다.
              </p>
            </div>
          </div>
        </div>

        <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4">
          <div className="flex">
            <div className="flex-shrink-0">
              <span className="text-yellow-400">⚠️</span>
            </div>
            <div className="ml-3">
              <p className="text-sm text-yellow-700">
                경고: 이것은 테스트용 더미 페이지입니다.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Home; 