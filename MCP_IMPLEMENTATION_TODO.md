# MCP 구현 TODO 리스트

## 📋 전체 구현 계획 (업데이트됨)

### 1단계: 기본 구조 활성화 (완료)

- [x] **1-1. 메시지 클래스들 생성**
  - [x] McpMessage.java 생성
  - [x] McpRequest.java 생성
  - [x] McpResponse.java 생성
- [x] **1-2. MCP 도구 인터페이스 활성화**
  - [x] McpTool.java 주석 제거 및 활성화
- [x] **1-3. WebSocket 설정**
  - [x] 별도 `McpSocketConfig`로 `/mcp` 네이티브 WS 엔드포인트 등록
  - [x] `SecurityConfig`에 `"/mcp"` permitAll 추가
  - [x] 초기 `McpGatewayHandler`로 연결 확인 → 이후 `McpServer`로 전환

### 2단계: 기본 도구 구현 (완료)

- [x] **2-1. PlayerStatsTool 구현/개선**
  - [x] `KboPlayerService` 기반 조회 플로우 (search → detail)
  - [x] 이름 기반 단일 입력 + 동명이인 후보 반환 + 후보 클릭 시 `playerId` 재조회
- [x] **2-2. 추가 도구들 구현**
  - [x] `TeamRankingTool` (현재 시즌 팀 순위)
  - [x] `GameScheduleTool` (오늘/지정일 경기 일정, null-safe)

### 3단계: MCP 서버 활성화 (완료)

- [x] **3-1. McpServer 활성화**
  - [x] Spring Bean 주입, `ObjectMapper`(JavaTimeModule 포함) 주입
  - [x] 도구 레지스트리 등록: getPlayerStats / getTeamRanking / getGameSchedule
- [x] **3-2. WebSocket 핸들러 교체 등록**
  - [x] `McpSocketConfig`에서 `McpServer` 생성자 주입으로 등록

### 4단계: 테스트 및 검증 (진행)

- [x] **4-1. WebSocket 연결/요청/응답 테스트** (hello, tools/list, 각 tool 정상 응답)
- [x] **4-1. 직렬화 이슈 해결** (Java 8 date/time 직렬화 오류 해결)
- [x] **4-1. 일정 조회 null 처리** (미입력 → 오늘 날짜 / 빈 배열 응답)
- [ ] **4-2. 통합 테스트 보강**
  - [ ] getPlayerStats: 정상/미존재/동명이인(후보 반환)/후보 재조회(playerId)
  - [ ] getTeamRanking: 데이터 스키마/로고 URL 유효성
  - [ ] getGameSchedule: 오늘/특정일/무자료/예외 케이스
  - [ ] 공통 에러 포맷(`error/detail`) 일관성 검증

### 5단계: 프론트엔드 연동 (완료)

- [x] **5-1. MCP 챗봇 UI 구현** (`frontend/src/pages/Chatbot.jsx`)
  - [x] tools/list 자동 로드, 메시지 스트림, 버튼/입력 폼 제공
  - [x] 응답 가독화(팀 순위 테이블/일정 카드/선수 스탯 카드/후보 리스트)
  - [x] 후보 클릭 시 자동 재조회
- [x] **5-2. 리소스/이미지 처리**
  - [x] 팀 로고 상대 경로 → 절대 경로 변환(`resolveLogoUrl`)
  - [x] `VITE_API_BASE_URL` 지원 (로컬/배포 전환 용이)

### 6단계: AI 모델 연동 (보류)

- [ ] **6-1. AI 서비스 선택 및 연동** (OpenAI/Claude/Gemini/로컬 등)
- [ ] **6-2. 프롬프트 엔지니어링/도구 호출 자동화**

---

## 🚀 지금부터 남은 작업(우선순위 순)

1. **통합 테스트 보강**

- [ ] WebSocket 요청 시나리오 스크립트 정리 및 반복 테스트
- [ ] 각 도구 에러/경계값 케이스 추가(날짜 포맷, 누락 인자 등)
- [ ] 직렬화/스키마 유효성 점검(JSON 필드 유무/타입)

2. **UX 다듬기(선택, 체감 개선)**

- [ ] 로딩 인디케이터(도구 호출 중 버튼 비활성/스피너)
- [ ] 에러 토스트/알림(공통 에러 응답 매핑)
- [ ] 표/카드 열 레이블 개선 및 정렬/하이라이트(예: 상위 3팀 강조)

3. **선택적 추가 도구**

- [ ] getPlayerRanking: 타자/투수 Top N 지표
- [ ] getLiveGameState: 진행 중 경기 상황(SBO/스코어)
- [ ] getTeamRoster: 팀별 선수 목록(포지션/등번호)

4. **운영/구성(선택)**

- [ ] 팀 로고 URL 절대 경로를 백엔드에서 직접 내려 안정성 강화
- [ ] 환경 변수 문서화(로컬/배포 `VITE_API_BASE_URL` 예시)
- [ ] CORS/WS Origin 설정 점검(배포 전)

---

## 📝 참고

- 프론트 환경 변수 예시: `frontend/.env`
  - `VITE_API_BASE_URL=http://localhost:8080`
- 챗봇 페이지 경로: `/chatbot`
