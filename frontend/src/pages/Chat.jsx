function Chat() {
  return (
    <div className="container mx-auto p-8">
      <h1 className="text-3xl font-bold text-green-600 mb-6">💬 실시간 채팅</h1>
      
      <div className="bg-white rounded-lg shadow-md h-96 flex flex-col">
        {/* 채팅 헤더 */}
        <div className="bg-green-600 text-white p-4 rounded-t-lg">
          <h2 className="text-lg font-semibold">KBO 팬 채팅방</h2>
          <p className="text-green-100 text-sm">현재 127명이 참여 중</p>
        </div>
        
        {/* 채팅 메시지 영역 */}
        <div className="flex-1 p-4 overflow-y-auto space-y-4">
          <div className="flex items-start space-x-3">
            <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center text-white text-sm font-bold">
              김
            </div>
            <div className="flex-1">
              <div className="flex items-center space-x-2 mb-1">
                <span className="font-semibold text-gray-800">김팬</span>
                <span className="text-xs text-gray-500">14:23</span>
              </div>
              <p className="text-gray-700 bg-gray-100 p-3 rounded-lg">오늘 LG vs KIA 경기 정말 재미있었어요!</p>
            </div>
          </div>
          
          <div className="flex items-start space-x-3">
            <div className="w-8 h-8 bg-red-500 rounded-full flex items-center justify-center text-white text-sm font-bold">
              이
            </div>
            <div className="flex-1">
              <div className="flex items-center space-x-2 mb-1">
                <span className="font-semibold text-gray-800">이팬</span>
                <span className="text-xs text-gray-500">14:25</span>
              </div>
              <p className="text-gray-700 bg-gray-100 p-3 rounded-lg">김현수 홈런 정말 멋졌어요! 🏟️</p>
            </div>
          </div>
          
          <div className="flex items-start space-x-3">
            <div className="w-8 h-8 bg-purple-500 rounded-full flex items-center justify-center text-white text-sm font-bold">
              박
            </div>
            <div className="flex-1">
              <div className="flex items-center space-x-2 mb-1">
                <span className="font-semibold text-gray-800">박팬</span>
                <span className="text-xs text-gray-500">14:27</span>
              </div>
              <p className="text-gray-700 bg-gray-100 p-3 rounded-lg">내일 경기도 기대되네요. 어떤 팀이 이길까요?</p>
            </div>
          </div>
        </div>
        
        {/* 메시지 입력 영역 */}
        <div className="p-4 border-t">
          <div className="flex space-x-2">
            <input 
              type="text" 
              placeholder="메시지를 입력하세요..." 
              className="flex-1 border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-green-500"
            />
            <button className="bg-green-600 text-white px-6 py-2 rounded-lg hover:bg-green-700 transition-colors">
              전송
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Chat; 