# MCP 구현 TODO 리스트

## 📋 전체 구현 계획

### 1단계: 기본 구조 활성화 (우선순위: 높음)

- [x] **1-1. 메시지 클래스들 생성**
  - [x] McpMessage.java 생성
  - [x] McpRequest.java 생성
  - [x] McpResponse.java 생성
- [ ] **1-2. MCP 도구 인터페이스 활성화**
  - [ ] McpTool.java 주석 제거 및 활성화
- [ ] **1-3. WebSocket 설정 수정**
  - [ ] WebSocketConfig.java에 MCP 엔드포인트 추가
  - [ ] MCP 서버 핸들러 등록

### 2단계: 기본 도구 구현 (우선순위: 높음)

- [ ] **2-1. PlayerStatsTool 활성화**
  - [ ] PlayerStatsTool.java 주석 제거
  - [ ] PlayerService 연동 확인
  - [ ] 기본 테스트
- [ ] **2-2. 추가 도구들 구현**
  - [ ] TeamRankingTool.java 생성
  - [ ] GameScheduleTool.java 생성
  - [ ] PlayerRankingTool.java 생성

### 3단계: MCP 서버 활성화 (우선순위: 중간)

- [ ] **3-1. McpServer 활성화**
  - [ ] McpServer.java 주석 제거
  - [ ] Spring Bean으로 등록
- [ ] **3-2. WebSocket 핸들러 등록**
  - [ ] WebSocketConfig에 MCP 핸들러 추가

### 4단계: 테스트 및 검증 (우선순위: 중간)

- [ ] **4-1. 단위 테스트**
  - [ ] 각 도구별 테스트
  - [ ] WebSocket 연결 테스트
- [ ] **4-2. 통합 테스트**
  - [ ] 전체 플로우 테스트

### 5단계: 프론트엔드 연동 (우선순위: 낮음)

- [ ] **5-1. MCP 클라이언트 구현**
  - [ ] WebSocket 연결 로직
  - [ ] 메시지 송수신 처리
- [ ] **5-2. UI 연동**
  - [ ] 기존 Chatbot.jsx와 연동
  - [ ] 실시간 응답 처리

### 6단계: AI 모델 연동 (우선순위: 낮음)

- [ ] **6-1. AI 서비스 선택 및 연동**
  - [ ] OpenAI/Claude/Gemini 중 선택
  - [ ] API 연동 구현
- [ ] **6-2. 프롬프트 엔지니어링**
  - [ ] 도구 호출 프롬프트 작성
  - [ ] 응답 변환 로직

---

## 🚀 현재 진행 상황

### 완료된 작업

- [x] TODO 리스트 작성
- [x] 메시지 클래스 생성 (완료)

### 다음 작업

1. McpTool.java 인터페이스 활성화
2. WebSocketConfig.java 수정
3. PlayerStatsTool.java 활성화

---

## 📝 구현 노트

### 파일 위치

- 메시지 클래스: `api-server/src/main/java/com/Tiebreaker/service/mcp/`
- WebSocket 설정: `api-server/src/main/java/com/Tiebreaker/config/WebSocketConfig.java`
- MCP 도구들: `api-server/src/main/java/com/Tiebreaker/service/mcp/`

### 참고 사항

- 모든 MCP 관련 파일들이 현재 주석 처리되어 있음
- 기존 WebSocket 설정은 STOMP 기반으로 되어 있음
- MCP는 일반 WebSocket으로 구현 예정
