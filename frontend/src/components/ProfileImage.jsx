import React from 'react';

/**
 * ProfileImage 컴포넌트
 * 프로필 이미지를 표시하는 재사용 가능한 컴포넌트
 * 
 * @param {string} src - 이미지 URL
 * @param {string} alt - 이미지 대체 텍스트
 * @param {string} size - 이미지 크기 (sm, md, lg, xl)
 * @param {string} className - 추가 CSS 클래스
 */
const ProfileImage = ({ 
  src, 
  alt = "프로필", 
  size = "md", 
  className = "" 
}) => {
  // 크기별 클래스 매핑
  const sizeClasses = {
    sm: "w-6 h-6",
    md: "w-8 h-8", 
    lg: "w-12 h-12",
    xl: "w-16 h-16"
  };

  const sizeClass = sizeClasses[size] || sizeClasses.md;

  return (
    <img 
      src={src} 
      alt={alt} 
      className={`${sizeClass} rounded-full object-cover border-2 border-gray-200 ${className}`}
      onError={(e) => {
        e.target.src = '/images/profile-default.svg';
      }}
    />
  );
};

export default ProfileImage;
