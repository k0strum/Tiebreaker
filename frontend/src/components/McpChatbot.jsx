// import React, { useState, useRef, useEffect } from 'react';

// const McpChatbot = () => {
//   const [messages, setMessages] = useState([]);
//   const [inputMessage, setInputMessage] = useState('');
//   const [isConnected, setIsConnected] = useState(false);
//   const [isLoading, setIsLoading] = useState(false);
//   const wsRef = useRef(null);
//   const messagesEndRef = useRef(null);

//   // WebSocket 연결
//   useEffect(() => {
//     connectWebSocket();
//     return () => {
//       if (wsRef.current) {
//         wsRef.current.close();
//       }
//     };
//   }, []);

//   const connectWebSocket = () => {
//     const ws = new WebSocket('ws://localhost:8080/mcp');

//     ws.onopen = () => {
//       setIsConnected(true);
//       console.log('MCP WebSocket 연결됨');
//     };

//     ws.onmessage = (event) => {
//       const data = JSON.parse(event.data);
//       handleMcpMessage(data);
//     };

//     ws.onclose = () => {
//       setIsConnected(false);
//       console.log('MCP WebSocket 연결 끊어짐');
//     };

//     ws.onerror = (error) => {
//       console.error('MCP WebSocket 오류:', error);
//     };

//     wsRef.current = ws;
//   };

//   const handleMcpMessage = (data) => {
//     if (data.type === 'tool/result') {
//       // 도구 실행 결과 처리
//       const result = data.content;

//       if (result.error) {
//         addMessage('AI', `죄송합니다. ${result.error}`, 'error');
//       } else if (result.message) {
//         addMessage('AI', result.message, 'success');
//       } else {
//         addMessage('AI', JSON.stringify(result, null, 2), 'info');
//       }

//       setIsLoading(false);
//     }
//   };

//   const addMessage = (sender, content, type = 'normal') => {
//     const newMessage = {
//       id: Date.now(),
//       sender,
//       content,
//       type,
//       timestamp: new Date().toLocaleTimeString()
//     };

//     setMessages(prev => [...prev, newMessage]);
//   };

//   const sendMessage = async () => {
//     if (!inputMessage.trim() || !isConnected) return;

//     const userMessage = inputMessage.trim();
//     setInputMessage('');
//     addMessage('사용자', userMessage);
//     setIsLoading(true);

//     try {
//       // AI 모델에 메시지 전송 (실제로는 AI 서비스와 연동)
//       const response = await fetch('/api/chat', {
//         method: 'POST',
//         headers: {
//           'Content-Type': 'application/json',
//         },
//         body: JSON.stringify({
//           message: userMessage,
//           sessionId: 'user-session-id'
//         })
//       });

//       const data = await response.json();

//       if (data.toolCall) {
//         // MCP 도구 호출
//         const toolRequest = {
//           type: 'tool/call',
//           toolName: data.toolCall.name,
//           arguments: data.toolCall.arguments,
//           requestId: Date.now().toString()
//         };

//         wsRef.current.send(JSON.stringify(toolRequest));
//       } else {
//         // 일반 응답
//         addMessage('AI', data.response);
//         setIsLoading(false);
//       }

//     } catch (error) {
//       console.error('챗봇 오류:', error);
//       addMessage('AI', '죄송합니다. 오류가 발생했습니다.', 'error');
//       setIsLoading(false);
//     }
//   };

//   const handleKeyPress = (e) => {
//     if (e.key === 'Enter' && !e.shiftKey) {
//       e.preventDefault();
//       sendMessage();
//     }
//   };

//   // 자동 스크롤
//   useEffect(() => {
//     messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
//   }, [messages]);

//   return (
//     <div className="container mx-auto p-8">
//       <h1 className="text-3xl font-bold text-orange-600 mb-6">🤖 MCP AI 챗봇</h1>

//       <div className="bg-white rounded-lg shadow-md h-96 flex flex-col">
//         {/* 챗봇 헤더 */}
//         <div className="bg-orange-600 text-white p-4 rounded-t-lg">
//           <div className="flex items-center space-x-3">
//             <div className="w-8 h-8 bg-white rounded-full flex items-center justify-center">
//               <span className="text-orange-600 text-lg">🤖</span>
//             </div>
//             <div className="flex-1">
//               <h2 className="text-lg font-semibold">Tiebreaker MCP AI</h2>
//               <p className="text-orange-100 text-sm">
//                 {isConnected ? '연결됨' : '연결 중...'}
//               </p>
//             </div>
//           </div>
//         </div>

//         {/* 채팅 메시지 영역 */}
//         <div className="flex-1 p-4 overflow-y-auto space-y-4">
//           {messages.length === 0 && (
//             <div className="text-center text-gray-500 mt-8">
//               <p>안녕하세요! KBO 관련 질문을 해주세요.</p>
//               <p className="text-sm mt-2">예: "김현수 선수 올해 성적 어때?"</p>
//             </div>
//           )}

//           {messages.map((message) => (
//             <div
//               key={message.id}
//               className={`flex items-start space-x-3 ${message.sender === '사용자' ? 'justify-end' : ''
//                 }`}
//             >
//               {message.sender === 'AI' && (
//                 <div className="w-8 h-8 bg-orange-500 rounded-full flex items-center justify-center text-white text-sm">
//                   AI
//                 </div>
//               )}

//               <div className={`flex-1 ${message.sender === '사용자' ? 'text-right' : ''}`}>
//                 <div className={`flex items-center space-x-2 mb-1 ${message.sender === '사용자' ? 'justify-end' : ''
//                   }`}>
//                   <span className="font-semibold text-gray-800">{message.sender}</span>
//                   <span className="text-xs text-gray-500">{message.timestamp}</span>
//                 </div>
//                 <div className={`p-3 rounded-lg inline-block ${message.sender === '사용자'
//                     ? 'bg-blue-600 text-white'
//                     : message.type === 'error'
//                       ? 'bg-red-50 text-red-700 border-l-4 border-red-500'
//                       : message.type === 'success'
//                         ? 'bg-green-50 text-green-700 border-l-4 border-green-500'
//                         : 'bg-orange-50 text-gray-700 border-l-4 border-orange-500'
//                   }`}>
//                   <pre className="whitespace-pre-wrap font-sans">{message.content}</pre>
//                 </div>
//               </div>

//               {message.sender === '사용자' && (
//                 <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center text-white text-sm font-bold">
//                   나
//                 </div>
//               )}
//             </div>
//           ))}

//           {isLoading && (
//             <div className="flex items-start space-x-3">
//               <div className="w-8 h-8 bg-orange-500 rounded-full flex items-center justify-center text-white text-sm">
//                 AI
//               </div>
//               <div className="flex-1">
//                 <div className="flex items-center space-x-2 mb-1">
//                   <span className="font-semibold text-gray-800">AI</span>
//                 </div>
//                 <div className="bg-orange-50 p-3 rounded-lg border-l-4 border-orange-500">
//                   <div className="flex space-x-1">
//                     <div className="w-2 h-2 bg-orange-500 rounded-full animate-bounce"></div>
//                     <div className="w-2 h-2 bg-orange-500 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
//                     <div className="w-2 h-2 bg-orange-500 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
//                   </div>
//                 </div>
//               </div>
//             </div>
//           )}

//           <div ref={messagesEndRef} />
//         </div>

//         {/* 메시지 입력 영역 */}
//         <div className="p-4 border-t">
//           <div className="flex space-x-2">
//             <input
//               type="text"
//               value={inputMessage}
//               onChange={(e) => setInputMessage(e.target.value)}
//               onKeyPress={handleKeyPress}
//               placeholder="AI에게 질문하세요..."
//               className="flex-1 border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-orange-500"
//               disabled={!isConnected || isLoading}
//             />
//             <button
//               onClick={sendMessage}
//               disabled={!isConnected || isLoading || !inputMessage.trim()}
//               className="bg-orange-600 text-white px-6 py-2 rounded-lg hover:bg-orange-700 transition-colors disabled:bg-gray-400"
//             >
//               전송
//             </button>
//           </div>
//         </div>
//       </div>

//       {/* 추천 질문 */}
//       <div className="mt-6 bg-gray-50 rounded-lg p-4">
//         <h3 className="font-semibold text-gray-800 mb-3">💡 추천 질문</h3>
//         <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
//           {[
//             "김현수 선수 올해 성적 어때?",
//             "LG 팀 순위는 어때?",
//             "오늘 경기 일정 알려줘",
//             "류현진 선수 방어율은?"
//           ].map((question, index) => (
//             <button
//               key={index}
//               onClick={() => {
//                 setInputMessage(question);
//                 // 자동으로 전송
//                 setTimeout(() => {
//                   setInputMessage(question);
//                   sendMessage();
//                 }, 100);
//               }}
//               className="text-left p-2 bg-white rounded border hover:bg-gray-50 transition-colors text-sm"
//             >
//               "{question}"
//             </button>
//           ))}
//         </div>
//       </div>
//     </div>
//   );
// };

// export default McpChatbot;
