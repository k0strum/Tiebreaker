function Chatbot() {
  return (
    <div className="container mx-auto p-8">
      <h1 className="text-3xl font-bold text-orange-600 mb-6">🤖 AI 챗봇</h1>
      
      <div className="bg-white rounded-lg shadow-md h-96 flex flex-col">
        {/* 챗봇 헤더 */}
        <div className="bg-orange-600 text-white p-4 rounded-t-lg">
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 bg-white rounded-full flex items-center justify-center">
              <span className="text-orange-600 text-lg">🤖</span>
            </div>
            <div>
              <h2 className="text-lg font-semibold">Tiebreaker AI 어시스턴트</h2>
              <p className="text-orange-100 text-sm">KBO 관련 질문에 답변해드립니다</p>
            </div>
          </div>
        </div>
        
        {/* 채팅 메시지 영역 */}
        <div className="flex-1 p-4 overflow-y-auto space-y-4">
          {/* AI 메시지 */}
          <div className="flex items-start space-x-3">
            <div className="w-8 h-8 bg-orange-500 rounded-full flex items-center justify-center text-white text-sm">
              AI
            </div>
            <div className="flex-1">
              <div className="flex items-center space-x-2 mb-1">
                <span className="font-semibold text-gray-800">Tiebreaker AI</span>
                <span className="text-xs text-gray-500">14:20</span>
              </div>
              <p className="text-gray-700 bg-orange-50 p-3 rounded-lg border-l-4 border-orange-500">
                안녕하세요! KBO 팬 여러분을 위한 AI 어시스턴트입니다. 
                선수 기록, 경기 일정, 팀 순위 등 궁금한 것이 있으시면 언제든 물어보세요! ⚾
              </p>
            </div>
          </div>
          
          {/* 사용자 메시지 */}
          <div className="flex items-start space-x-3 justify-end">
            <div className="flex-1 text-right">
              <div className="flex items-center space-x-2 mb-1 justify-end">
                <span className="text-xs text-gray-500">14:22</span>
                <span className="font-semibold text-gray-800">나</span>
              </div>
              <p className="text-white bg-blue-600 p-3 rounded-lg inline-block">
                김현수 선수 올해 성적이 어때요?
              </p>
            </div>
            <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center text-white text-sm font-bold">
              나
            </div>
          </div>
          
          {/* AI 응답 */}
          <div className="flex items-start space-x-3">
            <div className="w-8 h-8 bg-orange-500 rounded-full flex items-center justify-center text-white text-sm">
              AI
            </div>
            <div className="flex-1">
              <div className="flex items-center space-x-2 mb-1">
                <span className="font-semibold text-gray-800">Tiebreaker AI</span>
                <span className="text-xs text-gray-500">14:23</span>
              </div>
              <p className="text-gray-700 bg-orange-50 p-3 rounded-lg border-l-4 border-orange-500">
                김현수 선수의 2024시즌 성적입니다:
                <br/>• 타율: .345 (리그 1위)
                <br/>• 홈런: 25개 (리그 2위)
                <br/>• 타점: 89개 (리그 1위)
                <br/>• 출루율: .412
                <br/>• 장타율: .567
                <br/><br/>전반적으로 매우 좋은 성적을 보이고 있습니다! 🏆
              </p>
            </div>
          </div>
          
          {/* 사용자 메시지 */}
          <div className="flex items-start space-x-3 justify-end">
            <div className="flex-1 text-right">
              <div className="flex items-center space-x-2 mb-1 justify-end">
                <span className="text-xs text-gray-500">14:25</span>
                <span className="font-semibold text-gray-800">나</span>
              </div>
              <p className="text-white bg-blue-600 p-3 rounded-lg inline-block">
                오늘 경기 일정 알려줘
              </p>
            </div>
            <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center text-white text-sm font-bold">
              나
            </div>
          </div>
        </div>
        
        {/* 메시지 입력 영역 */}
        <div className="p-4 border-t">
          <div className="flex space-x-2">
            <input 
              type="text" 
              placeholder="AI에게 질문하세요..." 
              className="flex-1 border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-orange-500"
            />
            <button className="bg-orange-600 text-white px-6 py-2 rounded-lg hover:bg-orange-700 transition-colors">
              전송
            </button>
          </div>
        </div>
      </div>
      
      {/* 추천 질문 */}
      <div className="mt-6 bg-gray-50 rounded-lg p-4">
        <h3 className="font-semibold text-gray-800 mb-3">💡 추천 질문</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
          <button className="text-left p-2 bg-white rounded border hover:bg-gray-50 transition-colors text-sm">
            "LG 팀 순위는 어때요?"
          </button>
          <button className="text-left p-2 bg-white rounded border hover:bg-gray-50 transition-colors text-sm">
            "오늘 경기 일정 알려줘"
          </button>
          <button className="text-left p-2 bg-white rounded border hover:bg-gray-50 transition-colors text-sm">
            "류현진 선수 방어율은?"
          </button>
          <button className="text-left p-2 bg-white rounded border hover:bg-gray-50 transition-colors text-sm">
            "KBO 홈런 순위 보여줘"
          </button>
        </div>
      </div>
    </div>
  );
}

export default Chatbot; 