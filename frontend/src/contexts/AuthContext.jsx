import React, { createContext, useContext, useEffect, useState } from 'react';
import { jwtDecode } from 'jwt-decode';

/**
 * AuthContext - 전역 인증 상태 관리
 * 
 * 주요 역할:
 * 1. 로그인/로그아웃 상태 관리
 * 2. 사용자 정보 (프로필, 닉네임, 역할 등) 관리
 * 3. JWT 토큰 검증 및 디코딩
 * 4. 페이지 새로고침 시 인증 상태 복원
 * 5. localStorage와 React 상태 동기화
 */
const AuthContext = createContext();

/**
 * AuthProvider - 인증 관련 상태와 함수들을 제공하는 컴포넌트
 * 
 * @param {Object} children - 하위 컴포넌트들
 */
export const AuthProvider = ({ children }) => {
  // ===== 상태 관리 =====
  
  /**
   * 로그인 상태 - localStorage의 토큰 존재 여부로 초기화
   * !! 연산자로 boolean 값으로 변환
   */
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem('token'));
  
  /**
   * 프로필 이미지 - localStorage에서 가져오거나 기본 이미지 사용
   * 빈 문자열이거나 공백만 있는 경우 기본 이미지로 설정
   */
  const [profileImg, setProfileImg] = useState(() => {
    const savedImg = localStorage.getItem('member_ProfileImg');
    return savedImg && savedImg.trim() !== '' ? savedImg : '/images/profile-default.svg';
  });
  
  /**
   * 사용자 역할 - USER, ADMIN 등
   */
  const [role, setRole] = useState(localStorage.getItem('member_Role') || '');
  
  /**
   * 사용자 닉네임
   */
  const [nickname, setNickname] = useState(localStorage.getItem('member_Nickname') || '');
  
  /**
   * 사용자 이메일 - JWT 토큰에서 추출하거나 localStorage에서 가져옴
   */
  const [email, setEmail] = useState(localStorage.getItem('email') || null);
  
  /**
   * 사용자 ID - 백엔드에서 반환하는 고유 식별자
   */
  const [memberId, setMemberId] = useState(localStorage.getItem('memberId') || null);
  
  /**
   * 관리자 여부 - role이 'ADMIN'인지 확인
   */
  const [isAdmin, setIsAdmin] = useState(false);

  // ===== 로그인 함수 =====
  
  /**
   * 로그인 처리 함수
   * 
   * @param {string} token - JWT 토큰
   * @param {string} role - 사용자 역할
   * @param {string} profileImg - 프로필 이미지 URL
   * @param {string} nickname - 사용자 닉네임
   * @param {string|number} memberId - 사용자 고유 ID
   */
  const login = (token, role, profileImg, nickname, memberId) => {
    try {
      // JWT 토큰을 디코딩하여 사용자 정보 추출
      const decoded = jwtDecode(token);
      const userEmail = decoded.sub; // 토큰의 subject(이메일) 추출

      // localStorage에 모든 사용자 정보 저장
      localStorage.setItem('token', token);
      localStorage.setItem('member_Role', role);
      localStorage.setItem('member_ProfileImg', profileImg || '');
      localStorage.setItem('member_Nickname', nickname || '');
      localStorage.setItem('email', userEmail);
      localStorage.setItem('memberId', memberId || '');

      // React 상태 업데이트
      setIsLoggedIn(true);
      setRole(role);
      setNickname(nickname || '');
      
      // 프로필 이미지가 없거나 빈 문자열인 경우 기본 이미지 사용
      const finalProfileImg = profileImg && profileImg.trim() !== '' ? profileImg : '/images/profile-default.svg';
      setProfileImg(finalProfileImg);
      
      setEmail(userEmail);
      setMemberId(memberId);
      setIsAdmin(role === 'ADMIN');
      
    } catch (error) {
      console.error("로그인 처리 중 토큰 디코딩 실패", error);
      logout(); // 토큰 디코딩 실패 시 로그아웃 처리
    }
  };

  // ===== 로그아웃 함수 =====
  
  /**
   * 로그아웃 처리 함수
   * localStorage의 모든 인증 관련 데이터 삭제 및 상태 초기화
   */
  const logout = () => {
    // localStorage에서 모든 인증 관련 데이터 삭제
    localStorage.removeItem('token');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('member_Role');
    localStorage.removeItem('member_ProfileImg');
    localStorage.removeItem('member_Nickname');
    localStorage.removeItem('email');
    localStorage.removeItem('memberId');
    
    // React 상태 초기화
    setIsLoggedIn(false);
    setRole('');
    setProfileImg('/images/profile-default.svg');
    setNickname('');
    setEmail(null);
    setMemberId(null);
    setIsAdmin(false);
  };

  // ===== 페이지 새로고침 시 상태 복원 =====
  
  /**
   * 컴포넌트 마운트 시 실행되는 useEffect
   * 페이지 새로고침 시 localStorage의 정보로 인증 상태를 복원
   */
  useEffect(() => {
    const token = localStorage.getItem('token');
    
    if (token) {
      try {
        // JWT 토큰 유효성 검사
        const decoded = jwtDecode(token);
        
        // 토큰 만료 시간 확인 (exp는 초 단위, Date.now()는 밀리초 단위)
        if (decoded.exp * 1000 > Date.now()) {
          // 토큰이 유효하면 localStorage의 모든 정보로 상태 복원
          setIsLoggedIn(true);
          
          const savedRole = localStorage.getItem('member_Role') || '';
          setRole(savedRole);
          setIsAdmin(savedRole === 'ADMIN');
          
          setNickname(localStorage.getItem('member_Nickname') || '');
          setEmail(localStorage.getItem('email') || decoded.sub);
          setMemberId(localStorage.getItem('memberId') || null);
          
          const savedImg = localStorage.getItem('member_ProfileImg');
          setProfileImg(savedImg && savedImg.trim() !== '' ? savedImg : '/images/profile-default.svg');
          
        } else {
          // 토큰이 만료되었으면 로그아웃 처리
          console.log('토큰이 만료되어 로그아웃 처리합니다.');
          logout();
        }
      } catch (error) {
        console.error("페이지 로드 시 토큰 처리 오류", error);
        logout(); // 토큰 처리 오류 시 로그아웃 처리
      }
    }
  }, []); // 빈 배열로 한 번만 실행

  // ===== Context Provider 반환 =====
  
  /**
   * AuthContext.Provider로 모든 인증 관련 상태와 함수를 제공
   * 하위 컴포넌트에서 useAuth() 훅으로 접근 가능
   */
  return (
    <AuthContext.Provider value={{ 
      // 상태들
      isLoggedIn,    // 로그인 여부
      profileImg,    // 프로필 이미지
      role,          // 사용자 역할
      nickname,      // 닉네임
      email,         // 이메일
      memberId,      // 사용자 ID
      isAdmin,       // 관리자 여부
      
      // 함수들
      login,         // 로그인 함수
      logout         // 로그아웃 함수
    }}>
      {children}
    </AuthContext.Provider>
  );
};

/**
 * useAuth - AuthContext를 사용하기 위한 커스텀 훅
 * 
 * 사용법:
 * const { isLoggedIn, login, logout, nickname } = useAuth();
 * 
 * @returns {Object} 인증 관련 상태와 함수들
 */
export const useAuth = () => useContext(AuthContext); 