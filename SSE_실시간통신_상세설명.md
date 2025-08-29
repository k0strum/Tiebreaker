# SSE(Server-Sent Events) 실시간 통신 시스템 상세 설명

## 📋 목차

1. [SSE란 무엇인가?](#1-sse란-무엇인가)
2. [전체 시스템 아키텍처](#2-전체-시스템-아키텍처)
3. [핵심 컴포넌트 상세 분석](#3-핵심-컴포넌트-상세-분석)
4. [데이터 흐름 상세 설명](#4-데이터-흐름-상세-설명)
5. [실제 동작 과정](#5-실제-동작-과정)
6. [WebSocket vs SSE 비교](#6-websocket-vs-sse-비교)
7. [에러 처리 및 예외 상황](#7-에러-처리-및-예외-상황)
8. [성능 최적화 고려사항](#8-성능-최적화-고려사항)

---

## 1. SSE란 무엇인가?

### 1.1 SSE의 정의

**Server-Sent Events (SSE)**는 웹 브라우저가 서버로부터 실시간으로 데이터를 받을 수 있게 해주는 웹 표준 기술입니다.

### 1.2 주요 특징

- **단방향 통신**: 서버 → 클라이언트 (읽기 전용)
- **HTTP 기반**: 기존 HTTP 프로토콜 사용
- **자동 재연결**: 연결이 끊어지면 자동으로 재연결 시도
- **브라우저 내장 지원**: 별도 라이브러리 없이 사용 가능

### 1.3 SSE vs WebSocket vs Polling

| 구분            | SSE             | WebSocket | Polling         |
| --------------- | --------------- | --------- | --------------- |
| **통신 방향**   | 서버→클라이언트 | 양방향    | 클라이언트→서버 |
| **프로토콜**    | HTTP            | WebSocket | HTTP            |
| **연결 유지**   | 자동 재연결     | 수동 관리 | 매번 새 연결    |
| **실시간성**    | 높음            | 매우 높음 | 낮음            |
| **구현 복잡도** | 낮음            | 높음      | 매우 낮음       |

---

## 2. 전체 시스템 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Python 수집기  │    │   Kafka 브로커   │    │  Spring Boot    │
│                 │    │                 │    │                 │
│ • 외부 데이터    │───▶│ • 토픽:         │───▶│ • Kafka Consumer│
│   크롤링         │    │   'commentary'  │    │ • DB 저장       │
│ • JSON 직렬화    │    │ • 메시지 큐     │    │ • SSE 브로드캐스트│
│ • 2초마다 전송   │     │ • 순서 보장     │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                           │
                                                           ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React 프론트   │◀───│   SSE 연결      │◀───│   브라우저      │
│                 │    │                 │    │                 │
│ • 실시간 표시    │    │ • EventSource   │    │ • HTTP 연결     │
│ • 상태 관리      │    │ • 자동 재연결   │    │ • 이벤트 수신   │
│ • UI 업데이트    │    │ • 이벤트 파싱   │    │ • 렌더링        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

---

## 3. 핵심 컴포넌트 상세 분석

### 3.1 Python 수집기 (data-collector/collectors/commentary_collector.py)

```python
class CommentaryCollector:
    def run_once(self, source_game_id: str, our_game_id: str):
        # 1. 외부 데이터 수집
        raws = self.client.fetch_events(source_game_id, last_event_id)

        # 2. 데이터 정규화
        event = normalize_event(our_game_id, raw)

        # 3. JSON 직렬화하여 Kafka로 전송
        payload = {
            "gameId": event["gameId"],
            "ts": event["timestamp"],
            "text": event["text"],
            "severity": "INFO",
            "inning": event.get("inning"),
            "half": event.get("half"),
        }
        producer.send(self.kafka_topic, payload)
```

**핵심 역할:**

- 외부 야구 데이터 사이트에서 실시간 데이터 크롤링
- 데이터 정규화 및 JSON 형태로 변환
- Kafka 토픽으로 메시지 전송

### 3.2 Kafka Consumer (CommentaryConsumer.java)

```java
@KafkaListener(topics = "livegame", groupId = "tiebreaker-livegame")
public void onMessage(String message) {
    try {
        // 1. JSON 문자열을 DTO로 파싱
        CommentaryEvent event = objectMapper.readValue(message, CommentaryEvent.class);

        // 2. DB에 저장
        Commentary saved = commentaryService.save(event);

        // 3. SSE로 브로드캐스트
        commentaryService.broadcast(saved);

        System.out.println("✅ 해설 데이터 처리 완료: " + event.getText());
    } catch (JsonProcessingException e) {
        System.err.println("❌ Error parsing - Commentary JSON String: " + e.getMessage());
    }
}
```

**핵심 역할:**

- Kafka 토픽에서 메시지 수신
- JSON 파싱 및 비즈니스 로직 처리
- DB 저장 및 SSE 브로드캐스트 트리거

### 3.3 CommentaryService.java (핵심!)

```java
@Service
public class CommentaryService {
    // gameId별로 SSE 연결을 관리하는 맵
    private final Map<String, Set<SseEmitter>> emitterByGame = new ConcurrentHashMap<>();

    // 1. SSE 구독 등록
    public SseEmitter subscribe(String gameId) {
        SseEmitter emitter = new SseEmitter(0L); // 무제한 타임아웃

        // 해당 경기의 구독자 목록에 추가
        emitterByGame.computeIfAbsent(gameId, k -> Collections.synchronizedSet(new HashSet<>()))
            .add(emitter);

        // 연결 종료 시 정리 작업
        emitter.onCompletion(() -> removeEmitter(gameId, emitter));
        emitter.onTimeout(() -> removeEmitter(gameId, emitter));
        emitter.onError(e -> removeEmitter(gameId, emitter));

        // 초기 연결 확인 메시지 전송
        emitter.send(SseEmitter.event().name("init").data("ok"));

        return emitter;
    }

    // 2. 브로드캐스트 (핵심!)
    public void broadcast(Commentary c) {
        // 해당 경기의 모든 구독자에게 메시지 전송
        Set<SseEmitter> emitters = emitterByGame.getOrDefault(c.getGameId(), Collections.emptySet());
        List<SseEmitter> toRemove = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                // SSE 이벤트 전송
                emitter.send(SseEmitter.event()
                    .name("livegame")
                    .data(Map.of(
                        "gameId", c.getGameId(),
                        "ts", c.getTs(),
                        "text", c.getText(),
                        "severity", c.getSeverity(),
                        "inning", c.getInning(),
                        "half", c.getHalf())));
            } catch (IOException e) {
                // 연결이 끊어진 구독자 제거
                toRemove.add(emitter);
            }
        }

        // 끊어진 연결 정리
        toRemove.forEach(em -> removeEmitter(c.getGameId(), em));
    }
}
```

**핵심 개념:**

- **ConcurrentHashMap**: 스레드 안전한 구독자 관리
- **SseEmitter**: Spring의 SSE 구현체
- **이벤트 기반**: 연결/해제/에러 시 자동 정리

### 3.4 CommentaryController.java

```java
@RestController
@RequestMapping("/api")
public class CommentaryController {

    // REST API: 최근 해설 조회
    @GetMapping("/games/{gameId}/livegame")
    public Page<Commentary> list(
        @PathVariable String gameId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        return commentaryService.listRecent(gameId, page, size);
    }

    // SSE 엔드포인트: 실시간 스트림 구독
    @GetMapping(value = "/sse/games/{gameId}/livegame", produces = "text/event-stream")
    public SseEmitter sse(@PathVariable String gameId) {
        return commentaryService.subscribe(gameId);
    }
}
```

**핵심 역할:**

- REST API: 과거 데이터 조회
- SSE 엔드포인트: 실시간 스트림 구독

### 3.5 React 프론트엔드 (useSSECommentary.js)

```javascript
export const useSSECommentary = (gameId, baseUrl = "/api") => {
  const [events, setEvents] = useState([]);
  const [status, setStatus] = useState("disconnected");
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!gameId) return;

    // EventSource 생성 (브라우저 내장 API)
    const eventSource = new EventSource(
      `${baseUrl}/sse/games/${gameId}/commentary`
    );

    // 연결 성공
    eventSource.onopen = () => {
      setStatus("connected");
      setError(null);
    };

    // 이벤트 수신
    eventSource.onmessage = (event) => {
      const data = JSON.parse(event.data);
      setEvents((prev) => [data, ...prev]);
    };

    // 에러 처리
    eventSource.onerror = (error) => {
      setStatus("error");
      setError(error);
    };

    // 컴포넌트 언마운트 시 연결 해제
    return () => {
      eventSource.close();
    };
  }, [gameId, baseUrl]);

  return { events, status, error };
};
```

**핵심 개념:**

- **EventSource**: 브라우저 내장 SSE 클라이언트
- **자동 재연결**: 연결이 끊어지면 자동으로 재연결
- **이벤트 리스너**: onopen, onmessage, onerror

---

## 4. 데이터 흐름 상세 설명

### 4.1 전체 데이터 흐름

```
1. Python 수집기 (2초마다)
   ↓
2. 외부 데이터 크롤링
   ↓
3. JSON 직렬화
   ↓
4. Kafka 토픽 'commentary'로 전송
   ↓
5. Spring Boot Kafka Consumer 수신
   ↓
6. JSON 파싱 → CommentaryEvent DTO
   ↓
7. DB 저장 (Commentary 엔티티)
   ↓
8. CommentaryService.broadcast() 호출
   ↓
9. 해당 gameId의 모든 SseEmitter에게 이벤트 전송
   ↓
10. 브라우저 EventSource가 이벤트 수신
    ↓
11. React 상태 업데이트
    ↓
12. UI 실시간 렌더링
```

### 4.2 SSE 이벤트 형식

**서버에서 전송하는 이벤트:**

```
event: init
data: ok

event: commentary
data: {"gameId":"DEMO-GAME","ts":1733123456789,"text":"더미 이벤트","severity":"INFO","inning":1,"half":"T"}
```

**브라우저에서 수신:**

```javascript
// init 이벤트
eventSource.addEventListener("init", (event) => {
  console.log("SSE 연결 성공:", event.data); // "ok"
});

// commentary 이벤트
eventSource.addEventListener("commentary", (event) => {
  const data = JSON.parse(event.data);
  console.log("새 해설:", data);
});
```

---

## 5. 실제 동작 과정

### 5.1 연결 설정 과정

```
1. 브라우저에서 /api/sse/games/DEMO-GAME/commentary 접속
   ↓
2. CommentaryController.sse() 호출
   ↓
3. CommentaryService.subscribe() 호출
   ↓
4. SseEmitter 생성 및 emitterByGame 맵에 등록
   ↓
5. 초기 연결 확인 메시지 전송 (event: init)
   ↓
6. 브라우저 EventSource가 연결 성공 인식
```

### 5.2 메시지 브로드캐스트 과정

```
1. Python 수집기가 Kafka로 메시지 전송
   ↓
2. CommentaryConsumer.onMessage() 호출
   ↓
3. JSON 파싱 및 DB 저장
   ↓
4. commentaryService.broadcast() 호출
   ↓
5. emitterByGame에서 해당 gameId의 모든 SseEmitter 조회
   ↓
6. 각 SseEmitter에게 이벤트 전송
   ↓
7. 브라우저 EventSource가 이벤트 수신
   ↓
8. React 상태 업데이트 및 UI 렌더링
```

### 5.3 연결 해제 과정

```
1. 브라우저 탭 닫기 또는 페이지 새로고침
   ↓
2. EventSource 연결 해제
   ↓
3. SseEmitter.onCompletion() 또는 onError() 호출
   ↓
4. removeEmitter() 호출
   ↓
5. emitterByGame 맵에서 해당 SseEmitter 제거
   ↓
6. 메모리 정리 완료
```

---

## 6. WebSocket vs SSE 비교

### 6.1 언제 SSE를 사용하는가?

**SSE 사용이 적합한 경우:**

- ✅ 서버에서 클라이언트로의 단방향 통신
- ✅ 실시간 알림, 업데이트, 스트리밍
- ✅ 간단한 구현이 필요한 경우
- ✅ HTTP 기반 인프라 활용

**WebSocket 사용이 적합한 경우:**

- ✅ 양방향 실시간 통신 필요
- ✅ 채팅, 게임, 협업 도구
- ✅ 복잡한 실시간 상호작용

### 6.2 우리 프로젝트에서 SSE를 선택한 이유

1. **문자중계 특성**: 서버에서 클라이언트로의 단방향 전송
2. **구현 복잡도**: WebSocket보다 간단한 구현
3. **브라우저 호환성**: 별도 라이브러리 불필요
4. **자동 재연결**: 네트워크 문제 시 자동 복구

---

## 7. 에러 처리 및 예외 상황

### 7.1 주요 예외 상황들

**1. SSE 연결 끊김**

```java
try {
    emitter.send(event);
} catch (IOException e) {
    // 브라우저 탭 닫기, 새로고침 등
    toRemove.add(emitter);
}
```

**2. Kafka 메시지 파싱 실패**

```java
try {
    CommentaryEvent event = objectMapper.readValue(message, CommentaryEvent.class);
} catch (JsonProcessingException e) {
    System.err.println("❌ Error parsing - Commentary JSON String: " + e.getMessage());
}
```

**3. 브라우저 네트워크 문제**

```javascript
eventSource.onerror = (error) => {
  setStatus("error");
  setError(error);
  // EventSource는 자동으로 재연결 시도
};
```

### 7.2 에러 처리 전략

1. **연결 끊김**: 자동 재연결 (EventSource 내장 기능)
2. **메시지 파싱 실패**: 로그 기록 후 다음 메시지 처리
3. **서버 에러**: 클라이언트에서 재연결 시도
4. **메모리 누수 방지**: 연결 해제 시 즉시 정리

---

## 8. 성능 최적화 고려사항

### 8.1 현재 구현의 장점

1. **ConcurrentHashMap**: 스레드 안전한 구독자 관리
2. **이벤트 기반 정리**: 연결 해제 시 자동 정리
3. **페이징 조회**: 과거 데이터 효율적 조회
4. **메모리 효율성**: 끊어진 연결 즉시 제거

### 8.2 향후 개선 방향

1. **Redis Pub/Sub**: 대규모 사용자 지원
2. **메시지 압축**: 네트워크 대역폭 절약
3. **연결 풀링**: 서버 리소스 효율적 사용
4. **모니터링**: 연결 수, 메시지 처리량 추적

---

## 🎯 핵심 포인트 정리

### 1. SSE의 핵심 개념

- **단방향 실시간 통신**
- **HTTP 기반, 자동 재연결**
- **브라우저 내장 지원**

### 2. 우리 시스템의 핵심

- **Kafka**: 메시지 큐로 안정적 전달
- **SseEmitter**: Spring의 SSE 구현체
- **ConcurrentHashMap**: 스레드 안전한 구독자 관리
- **EventSource**: 브라우저 SSE 클라이언트

### 3. 데이터 흐름

```
Python 수집기 → Kafka → Spring Boot → SSE → React → UI
```

### 4. 에러 처리

- **연결 끊김**: 자동 재연결
- **파싱 실패**: 로그 기록 후 계속 진행
- **메모리 누수**: 즉시 정리

이 시스템은 실시간 문자중계를 위한 완벽한 아키텍처를 제공합니다! 🚀
