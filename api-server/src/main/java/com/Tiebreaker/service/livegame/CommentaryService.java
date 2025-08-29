package com.Tiebreaker.service.livegame;

import com.Tiebreaker.dto.livegame.CommentaryEvent;
import com.Tiebreaker.entity.livegame.Commentary;
import com.Tiebreaker.repository.CommentaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CommentaryService {

  private final CommentaryRepository commentaryRepository;

  // gameId -> emitters
  private final Map<String, Set<SseEmitter>> emitterByGame = new ConcurrentHashMap<>();

  public Commentary save(CommentaryEvent event) {
    Commentary c = Commentary.builder()
        .gameId(event.getGameId())
        .ts(event.getTs())
        .text(event.getText())
        .severity(event.getSeverity())
        .inning(event.getInning())
        .half(event.getHalf())
        .build();

    Commentary saved = commentaryRepository.save(c);
    System.out.println("✅ 해설 데이터 저장 완료: " + event.getText());
    return saved;
  }

  public Page<Commentary> listRecent(String gameId, int page, int size) {
    return commentaryRepository.findByGameIdOrderByTsDesc(gameId, PageRequest.of(page, size));
  }

  public SseEmitter subscribe(String gameId) {
    SseEmitter emitter = new SseEmitter(0L); // no timeout; rely on proxy timeouts
    emitterByGame.computeIfAbsent(gameId, k -> Collections.synchronizedSet(new HashSet<>()))
        .add(emitter);

    emitter.onCompletion(() -> removeEmitter(gameId, emitter));
    emitter.onTimeout(() -> removeEmitter(gameId, emitter));
    emitter.onError(e -> removeEmitter(gameId, emitter));

    try {
      emitter.send(SseEmitter.event().name("init").data("ok"));
    } catch (IOException ignored) {
    }

    return emitter;
  }

  public void broadcast(Commentary c) {
    Set<SseEmitter> emitters = emitterByGame.getOrDefault(c.getGameId(), Collections.emptySet());
    List<SseEmitter> toRemove = new ArrayList<>();

    for (SseEmitter emitter : emitters) {
      try {
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
        // SSE 연결이 끊어진 경우 (브라우저 탭 닫기, 새로고침 등)
        // 정상적인 상황이므로 에러 로그 대신 디버그 로그로 처리
        System.out.println("🔌 SSE 연결 종료");
        toRemove.add(emitter);
      } catch (IllegalStateException e) {
        // 이미 완료된 emitter
        System.out.println("🔌 SSE 연결 이미 종료됨");
        toRemove.add(emitter);
      }
    }

    toRemove.forEach(em -> removeEmitter(c.getGameId(), em));

    if (!emitters.isEmpty()) {
      System.out.println("✅ 해설 브로드캐스트 완료: " + c.getText() + " (구독자: " + emitters.size() + "명)");
    }
  }

  private void removeEmitter(String gameId, SseEmitter emitter) {
    Set<SseEmitter> set = emitterByGame.get(gameId);
    if (set != null) {
      set.remove(emitter);
      if (set.isEmpty()) {
        emitterByGame.remove(gameId);
      }
    }
  }
}
