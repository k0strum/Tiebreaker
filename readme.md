# <a id="top"></a>⚾ KBO 팬 플랫폼 프로젝트 Tiebreaker
<table>
  <tr>
    <td>
      Notion
    </td>
    <td>
      https://www.notion.so/Personal-Project-Tiebreaker-273237f77cdb81edab39f8e0f50dbe40   
    </td>
  </tr>
  <tr>
    <td>
      Portfolio
    </td>
    <td>
      <a href="https://github.com/k0strum/Tiebreaker/blob/main/gitFiles/tiebreaker_portfolio.pdf">Tiebreak_portfolio</a>
    </td>
  </tr>
</table>

### 목차
1.  [프로젝트 개요](#1-프로젝트-개요)
2.  [기술 스택](#2-기술-스택)
3.  [주요 기능](#3-주요-기능)
4.  [시스템 아키텍쳐](#4-시스템-아키텍쳐)   

## 1. 프로젝트 개요
>   흩어져 있는 KBO 경기 정보, 선수 기록, 팬 커뮤니티를 하나의 공간에 통합하고,   
>   실시간 소통과 데이터 기록의 재미를 더한 차세대 KBO 팬 플랫폼입니다.
## 2. 기술 스택

**A. Frontend (웹 UI/UX) - Vite + React, TailwindCSS**
   * **역할/선정 이유:** 빠른 개발 속도와 동적 렌더링을 제공. 다양한 기능의 화면을 효율적으로 구현합니다.

**B. Backend (메인 API 서버) - Spring Boot, JPA, Java**
   * **역할/선정 이유:** 안정적인 비즈니스 로직 처리, 세이버메트릭스 계산, 챗봇 등 핵심 기능을 담당합니다.

**C. Data Collector (데이터 수집 서버) - Python**
   * **역할/선정 이유:** 외부 사이트에서 경기 및 선수 데이터를 크롤링. 세이버메트릭스 계산에 필요한 원본 데이터를 수집합니다.

**D. Database (영구 데이터 저장) - MySQL**
   * **역할/선정 이유:** 선수/경기 기록, 회원 정보 등 정형 데이터를 저장합니다.

**E. Real-time (실시간 통신) - WebSocket, SSE**
   * **역할/선정 이유:** SSE는 스코어보드 정보를, WebSocket은 채팅을 클라이언트에 전달합니다.

**F. Inter-server (서버 간 통신) - Kafka**
   * **역할/선정 이유:** 데이터 수집 서버와 메인 서버 간 안정적인 비동기 데이터 파이프라인을 구축해 트래픽을 효율적으로 관리합니다.

## 3. 주요 기능

**A. 데이터/기록**
- 경기 일정 정보: 이전 경기의 결과, 진행 예정 경기 정보 등 경기 데이터 제공   
<img width="700" alt="Image" src="https://github.com/user-attachments/assets/69775e64-51ad-4433-a4a6-8cdb5ebbe281" /><br/>
- 선수 기록실: 시즌 성적, 개인 프로필 등 조회 및 월별/연도별 성적 추이 등 데이터 시각화 제공
<img width="700" alt="Image" src="https://github.com/user-attachments/assets/814ec876-b628-4683-a5ad-2968eabe42a8" /><br/>
<img width="700" height="593" alt="Image" src="https://github.com/user-attachments/assets/ef6f9385-081b-4666-a0d5-ea88f51ef837" /><br/>

**B. 실시간 경기 중계**
- 실시간 스코어보드: 현재 진행 중인 경기의 상황(스코어, 주자, SBO)실시간 제공
<img width="700" alt="Image" src="https://github.com/user-attachments/assets/ed95f2a3-642d-49ef-ace8-7a17be1b0f66" /><br/>
- 경기별 다중 채팅방: 각 경기마다 생성되는 실시간 채팅방에서 팬들 간의 응원 및 정보 교류   
<img width="700" height="285" alt="Image" src="https://github.com/user-attachments/assets/289bdac2-cb4c-401b-a53e-bfa5c151f0c4" /><br/>

**C. 챗봇**
- 야구 정보 챗봇: 선수 기록, 경기 일정 등 간단한 야구 정보를 실시간으로 확인하는 챗봇
<img width="700" alt="Image" src="https://github.com/user-attachments/assets/c2a1d5c0-145b-4b95-b9b9-ea16452d974d" /><br>

## 4. 시스템 아키텍쳐
<img width="800" alt="Image" src="https://github.com/user-attachments/assets/70e8ee1f-eefe-4b03-bdf9-b4149a4c13b9" />

[맨 위로 이동](#top)
