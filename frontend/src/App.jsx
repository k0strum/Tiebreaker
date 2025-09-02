import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom'
import './App.css'

// AuthContext import
import { AuthProvider } from './contexts/AuthContext.jsx'

// 컴포넌트 import
import Header from './components/Header.jsx'

// 페이지 컴포넌트들
import Home from './pages/Home.jsx'
import PlayerRanking from './pages/PlayerRanking.jsx'
import PlayerDetail from './pages/PlayerDetail.jsx'
import Chatbot from './pages/Chatbot.jsx'
import GameSchedule from './pages/GameSchedule.jsx'
import LiveGames from './pages/LiveGames.jsx'
import LiveGame from './pages/LiveGame.jsx'
import Login from './pages/auth/Login.jsx'
import Signup from './pages/auth/Signup.jsx'
import SignupComplete from './pages/auth/SignupComplete.jsx'
import EmailVerification from './pages/auth/EmailVerification.jsx'
import OAuthCallback from './pages/auth/OAuthCallback.jsx'

// 조건부 헤더를 사용하는 메인 컴포넌트
const AppContent = () => {
  const location = useLocation();

  // 헤더를 숨길 경로들
  const hideHeaderPaths = ['/login', '/signup', '/signup-complete', '/verify-email', '/reset-pw', '/admin', '/oauth-callback'];

  // 현재 경로가 헤더를 숨겨야 하는지 확인
  const shouldShowHeader = !hideHeaderPaths.includes(location.pathname);

  return (
    <div className="app-container">
      {shouldShowHeader && <Header />}

      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/signup-complete" element={<SignupComplete />} />
          <Route path="/verify-email" element={<EmailVerification />} />
          <Route path="/oauth-callback" element={<OAuthCallback />} />
          <Route path="/rankings" element={<PlayerRanking />} />
          <Route path="/player/:playerId" element={<PlayerDetail />} />
          <Route path="/chatbot" element={<Chatbot />} />
          <Route path="/games" element={<GameSchedule />} />
          <Route path="/live-games" element={<LiveGames />} />
          <Route path="/live-game/:gameId" element={<LiveGame />} />
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
