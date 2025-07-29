function PlayerStats() {
  return (
    <div className="container mx-auto p-8">
      <h1 className="text-3xl font-bold text-blue-600 mb-6">📊 선수 기록실</h1>
      
      <div className="bg-white rounded-lg shadow-md p-6">
        <div className="mb-6">
          <h2 className="text-xl font-semibold mb-4">타자 순위</h2>
          <div className="overflow-x-auto">
            <table className="min-w-full table-auto">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-2 text-left">순위</th>
                  <th className="px-4 py-2 text-left">선수명</th>
                  <th className="px-4 py-2 text-left">팀</th>
                  <th className="px-4 py-2 text-left">타율</th>
                  <th className="px-4 py-2 text-left">홈런</th>
                  <th className="px-4 py-2 text-left">타점</th>
                </tr>
              </thead>
              <tbody>
                <tr className="border-b">
                  <td className="px-4 py-2">1</td>
                  <td className="px-4 py-2">김현수</td>
                  <td className="px-4 py-2">LG</td>
                  <td className="px-4 py-2">.345</td>
                  <td className="px-4 py-2">25</td>
                  <td className="px-4 py-2">89</td>
                </tr>
                <tr className="border-b">
                  <td className="px-4 py-2">2</td>
                  <td className="px-4 py-2">이정후</td>
                  <td className="px-4 py-2">키움</td>
                  <td className="px-4 py-2">.338</td>
                  <td className="px-4 py-2">18</td>
                  <td className="px-4 py-2">76</td>
                </tr>
                <tr className="border-b">
                  <td className="px-4 py-2">3</td>
                  <td className="px-4 py-2">박병호</td>
                  <td className="px-4 py-2">KT</td>
                  <td className="px-4 py-2">.332</td>
                  <td className="px-4 py-2">30</td>
                  <td className="px-4 py-2">95</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        
        <div>
          <h2 className="text-xl font-semibold mb-4">투수 순위</h2>
          <div className="overflow-x-auto">
            <table className="min-w-full table-auto">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-2 text-left">순위</th>
                  <th className="px-4 py-2 text-left">선수명</th>
                  <th className="px-4 py-2 text-left">팀</th>
                  <th className="px-4 py-2 text-left">승</th>
                  <th className="px-4 py-2 text-left">패</th>
                  <th className="px-4 py-2 text-left">방어율</th>
                </tr>
              </thead>
              <tbody>
                <tr className="border-b">
                  <td className="px-4 py-2">1</td>
                  <td className="px-4 py-2">류현진</td>
                  <td className="px-4 py-2">한화</td>
                  <td className="px-4 py-2">12</td>
                  <td className="px-4 py-2">3</td>
                  <td className="px-4 py-2">2.15</td>
                </tr>
                <tr className="border-b">
                  <td className="px-4 py-2">2</td>
                  <td className="px-4 py-2">양현종</td>
                  <td className="px-4 py-2">KIA</td>
                  <td className="px-4 py-2">11</td>
                  <td className="px-4 py-2">5</td>
                  <td className="px-4 py-2">2.45</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}

export default PlayerStats; 