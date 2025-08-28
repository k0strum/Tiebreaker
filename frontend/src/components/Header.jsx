import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import ProfileImage from './ProfileImage';

const Header = () => {
  const { isLoggedIn, profileImg, nickname, logout } = useAuth();
  const location = useLocation();

  const navigationItems = [
    { path: '/', label: '홈' },
    { path: '/games', label: '경기일정' },
    { path: '/live-games', label: '실시간경기' },
    { path: '/rankings', label: '선수정보' },
    { path: '/predictions', label: '승부예측' },
    { path: '/chat', label: '실시간채팅' },
    { path: '/chatbot', label: 'AI챗봇' },
    { path: '/commentary-demo', label: '중계방(테스트)' },
  ];

  return (
    <div className="bg-white shadow-sm border-b">
      {/* Top Bar */}
      <div className="bg-white px-6 py-3 flex justify-between items-center">
        {/* Left side - Logo and Brand */}
        <div className="flex items-center space-x-3">
          <div className="w-8 h-8 bg-blue-600 rounded flex items-center justify-center">
            <span className="text-white font-bold text-lg">T</span>
          </div>
          <span className="text-black font-bold text-xl">타이브레이커</span>
        </div>

        {/* Right side - Search and Profile */}
        <div className="flex items-center space-x-4">
          {/* Search Icon */}
          <button className="p-2 hover:bg-gray-100 rounded-full transition-colors">
            <svg className="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </button>

          {/* Profile/Login */}
          {isLoggedIn ? (
            <div className="flex items-center space-x-3">
              {/* Profile Image */}
              <div className="flex items-center space-x-2">
                <ProfileImage src={profileImg} alt="프로필" size="md" />
                <span className="text-sm font-medium text-gray-700">
                  {nickname || '사용자'}
                </span>
              </div>
              <button
                onClick={logout}
                className="text-sm text-gray-600 hover:text-gray-800 transition-colors px-3 py-1 rounded-md hover:bg-gray-100"
              >
                로그아웃
              </button>
            </div>
          ) : (
            <Link
              to="/login"
              className="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center hover:bg-blue-700 transition-colors"
            >
              <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </Link>
          )}
        </div>
      </div>

      {/* Navigation Bar */}
      <nav className="bg-white px-6 py-3">
        <div className="flex space-x-8">
          {navigationItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={`text-sm font-bold transition-colors ${location.pathname === item.path
                ? 'text-blue-600'
                : 'text-gray-800 hover:text-blue-600'
                }`}
            >
              {item.label}
            </Link>
          ))}
        </div>
      </nav>
    </div>
  );
};

export default Header; 