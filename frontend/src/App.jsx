import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom'
import './App.css'

// AuthContext import
import { AuthProvider } from './contexts/AuthContext.jsx'

// 페이지 컴포넌트들 (나중에 별도 파일로 분리 가능)
import Home from './pages/Home.jsx'
import PlayerStats from './pages/PlayerStats.jsx'
import Chat from './pages/Chat.jsx'
import Predictions from './pages/Predictions.jsx'
import Chatbot from './pages/Chatbot.jsx'
import Login from './pages/auth/Login.jsx'
import Signup from './pages/auth/Signup.jsx'

// 헤더 컴포넌트 (나중에 별도 파일로 분리)
const Header = () => (
  <header className="bg-blue-600 text-white p-6 text-center">
    <h1 className="text-3xl font-bold">⚾ Tiebreaker</h1>
    <p className="text-blue-100 mt-2">KBO 팬 플랫폼</p>
  </header>
);

// 네비게이션 컴포넌트
const Navigation = () => (
  <nav className="bg-gray-800 text-white p-4">
    <ul className="flex justify-center space-x-8">
      <li><a href="/" className="hover:text-blue-300">홈</a></li>
      <li><a href="/stats" className="hover:text-blue-300">선수 기록실</a></li>
      <li><a href="/chat" className="hover:text-blue-300">실시간 채팅</a></li>
      <li><a href="/predictions" className="hover:text-blue-300">승부 예측</a></li>
      <li><a href="/chatbot" className="hover:text-blue-300">AI 챗봇</a></li>
    </ul>
  </nav>
);

// 조건부 헤더를 사용하는 메인 컴포넌트
const AppContent = () => {
  const location = useLocation();
  
  // 헤더를 숨길 경로들
  const hideHeaderPaths = ['/login', '/signup', '/reset-pw', '/admin'];
  
  // 현재 경로가 헤더를 숨겨야 하는지 확인
  const shouldShowHeader = !hideHeaderPaths.includes(location.pathname);

  return (
    <div className="app-container">
      {shouldShowHeader && <Header />}
      {shouldShowHeader && <Navigation />}
      
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/stats" element={<PlayerStats />} />
          <Route path="/chat" element={<Chat />} />
          <Route path="/predictions" element={<Predictions />} />
          <Route path="/chatbot" element={<Chatbot />} />
        </Routes>
      </main>
    </div>
  );
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <AppContent />
      </Router>
    </AuthProvider>
  )
}

export default App
