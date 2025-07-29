function Predictions() {
  return (
    <div className="container mx-auto p-8">
      <h1 className="text-3xl font-bold text-purple-600 mb-6">🎯 승부 예측</h1>
      
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* 오늘의 경기 */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold text-purple-600 mb-4">오늘의 경기</h2>
          
          <div className="space-y-4">
            <div className="border rounded-lg p-4">
              <div className="flex justify-between items-center mb-3">
                <span className="text-sm text-gray-500">2024.06.15 18:30</span>
                <span className="bg-green-100 text-green-800 px-2 py-1 rounded text-xs">진행중</span>
              </div>
              
              <div className="flex items-center justify-between">
                <div className="text-center flex-1">
                  <div className="text-lg font-bold">LG</div>
                  <div className="text-2xl font-bold text-blue-600">3</div>
                </div>
                
                <div className="text-center mx-4">
                  <div className="text-sm text-gray-500">VS</div>
                </div>
                
                <div className="text-center flex-1">
                  <div className="text-lg font-bold">KIA</div>
                  <div className="text-2xl font-bold text-red-600">2</div>
                </div>
              </div>
              
              <div className="mt-4 pt-4 border-t">
                <div className="flex justify-between text-sm">
                  <span>예측 참여: 1,234명</span>
                  <span>포인트: 100P</span>
                </div>
              </div>
            </div>
            
            <div className="border rounded-lg p-4">
              <div className="flex justify-between items-center mb-3">
                <span className="text-sm text-gray-500">2024.06.15 19:00</span>
                <span className="bg-gray-100 text-gray-800 px-2 py-1 rounded text-xs">예정</span>
              </div>
              
              <div className="flex items-center justify-between">
                <div className="text-center flex-1">
                  <div className="text-lg font-bold">SSG</div>
                  <div className="text-2xl font-bold text-gray-400">-</div>
                </div>
                
                <div className="text-center mx-4">
                  <div className="text-sm text-gray-500">VS</div>
                </div>
                
                <div className="text-center flex-1">
                  <div className="text-lg font-bold">두산</div>
                  <div className="text-2xl font-bold text-gray-400">-</div>
                </div>
              </div>
              
              <div className="mt-4 pt-4 border-t">
                <div className="flex justify-between text-sm">
                  <span>예측 참여: 856명</span>
                  <span>포인트: 100P</span>
                </div>
                <button className="w-full mt-2 bg-purple-600 text-white py-2 rounded-lg hover:bg-purple-700 transition-colors">
                  예측하기
                </button>
              </div>
            </div>
          </div>
        </div>
        
        {/* 내 예측 기록 */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold text-purple-600 mb-4">내 예측 기록</h2>
          
          <div className="space-y-4">
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
              <div>
                <div className="font-semibold">LG vs KIA</div>
                <div className="text-sm text-gray-600">LG 승리 예측</div>
              </div>
              <div className="text-right">
                <div className="text-green-600 font-bold">+50P</div>
                <div className="text-xs text-gray-500">정답</div>
              </div>
            </div>
            
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
              <div>
                <div className="font-semibold">KT vs NC</div>
                <div className="text-sm text-gray-600">KT 승리 예측</div>
              </div>
              <div className="text-right">
                <div className="text-red-600 font-bold">-20P</div>
                <div className="text-xs text-gray-500">오답</div>
              </div>
            </div>
            
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
              <div>
                <div className="font-semibold">삼성 vs 한화</div>
                <div className="text-sm text-gray-600">삼성 승리 예측</div>
              </div>
              <div className="text-right">
                <div className="text-green-600 font-bold">+30P</div>
                <div className="text-xs text-gray-500">정답</div>
              </div>
            </div>
          </div>
          
          <div className="mt-6 p-4 bg-purple-50 rounded-lg">
            <div className="text-center">
              <div className="text-2xl font-bold text-purple-600">1,250P</div>
              <div className="text-sm text-gray-600">총 보유 포인트</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Predictions; 