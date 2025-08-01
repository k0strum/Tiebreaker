import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom'
import './App.css'

// AuthContext import
import { AuthProvider } from './contexts/AuthContext.jsx'

// 컴포넌트 import
import Header from './components/Header.jsx'

// 페이지 컴포넌트들 (나중에 별도 파일로 분리 가능)
import Home from './pages/Home.jsx'
import PlayerStats from './pages/PlayerStats.jsx'
import Chat from './pages/Chat.jsx'
import Predictions from './pages/Predictions.jsx'
import Chatbot from './pages/Chatbot.jsx'
import Login from './pages/auth/Login.jsx'
// import Signup from './pages/auth/Signup.jsx'

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
      
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          {/* <Route path="/signup" element={<Signup />} /> */}
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
