package com.Tiebreaker.service.kboInfoCollecting.daily;

import com.Tiebreaker.entity.kboInfo.KboConstants;
import com.Tiebreaker.repository.kboInfo.KboConstantsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Year;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class KboConstantsService {

  private final KboConstantsRepository kboConstantsRepository;

  private static final String KBO_CONSTANTS_URL = "https://statiz.sporki.com/stats/?m=const";

  // 매일 새벽: 최신 연도 우선 갱신
  @Scheduled(cron = "0 0 2 * * *")
  public void scheduleDailyUpdate() {
    try {
      Document document = fetchDocument();
      Map<Integer, KboConstants> yearToConstants = parseConstants(document);

      int currentYear = Year.now().getValue();
      KboConstants latest = yearToConstants.get(currentYear);
      if (latest != null) {
        upsert(latest);
        log.info("[KBO Constants] {}년 상수 갱신 완료", currentYear);
      } else {
        log.warn("[KBO Constants] {}년 상수를 페이지에서 찾지 못했습니다", currentYear);
      }
    } catch (Exception e) {
      log.warn("[KBO Constants] 일일 갱신 실패: {}", e.getMessage());
    }
  }

  // 서버 기동 직후 1회 실행
  @EventListener(ApplicationReadyEvent.class)
  public void runOnceOnStartup() {
    scheduleDailyUpdate();
  }

  public Optional<Double> getFipConstant(int year) {
    return kboConstantsRepository.findById(year).map(KboConstants::getCFip);
  }

  private Document fetchDocument() throws IOException {
    return Jsoup.connect(KBO_CONSTANTS_URL)
        .userAgent(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")
        .timeout(8000)
        .get();
  }

  // 페이지에서 테이블을 파싱하여 연도→상수 매핑 생성
  private Map<Integer, KboConstants> parseConstants(Document document) {
    Element table = selectConstantsTable(document);
    if (table == null)
      throw new IllegalStateException("상수 테이블을 찾지 못했습니다");

    // 헤더 인덱스 매핑
    List<String> headers = new ArrayList<>();
    for (Element th : table.select("thead tr th")) {
      headers.add(normalizeHeader(th.text()));
    }

    Map<Integer, KboConstants> result = new LinkedHashMap<>();
    Elements rows = table.select("tbody tr");
    for (Element row : rows) {
      Elements tds = row.select("td");
      if (tds.isEmpty())
        continue;

      try {
        KboConstants constants = parseRow(headers, tds);
        if (constants != null) {
          result.put(constants.getYear(), constants);
        }
      } catch (Exception parseError) {
        log.warn("[KBO Constants] 행 파싱 실패: {}", parseError.getMessage());
      }
    }
    return result;
  }

  private Element selectConstantsTable(Document document) {
    // 테스트에서 사용한 경로 우선 시도
    Elements candidates = document.select("body > div.warp > div.container > section > div.box_type_boared table");
    if (!candidates.isEmpty())
      return candidates.first();
    // 폴백: 첫 번째 테이블 사용
    return document.selectFirst("table");
  }

  private String normalizeHeader(String headerText) {
    return headerText.trim().toUpperCase()
        .replace("/", "_") // R/ePA -> R_EPA
        .replace(" ", "");
  }

  private KboConstants parseRow(List<String> headers, Elements tds) {
    KboConstants c = new KboConstants();

    for (int i = 0; i < Math.min(headers.size(), tds.size()); i++) {
      String key = headers.get(i);
      String raw = tds.get(i).text().trim();

      switch (key) {
        case "YEAR":
          c.setYear(parseInt(raw));
          break;
        case "WOBA":
          c.setWoba(parseDouble(raw));
          break;
        case "SCALE":
          c.setScale(parseDouble(raw));
          break;
        case "EBB":
          c.setEbb(parseDouble(raw));
          break;
        case "1B":
          c.setSingles(parseDouble(raw));
          break;
        case "2B":
          c.setDoubles(parseDouble(raw));
          break;
        case "3B":
          c.setTriples(parseDouble(raw));
          break;
        case "HR":
          c.setHomeRuns(parseDouble(raw));
          break;
        case "SB2":
          c.setSb2(parseDouble(raw));
          break;
        case "SB3":
          c.setSb3(parseDouble(raw));
          break;
        case "CS2":
          c.setCs2(parseDouble(raw));
          break;
        case "CS3":
          c.setCs3(parseDouble(raw));
          break;
        case "R_EPA":
          c.setRunsPerEpa(parseDouble(raw));
          break;
        case "RPW":
          c.setRpw(parseDouble(raw));
          break;
        case "CFIP":
          c.setCFip(parseDouble(raw));
          break;
        default:
          // 알 수 없는 헤더는 무시
          break;
      }
    }

    if (c.getYear() == null)
      return null; // 연도 없으면 무시
    return c;
  }

  @Transactional
  protected void upsert(KboConstants incoming) {
    // 단순 업서트: 동일 연도는 덮어쓰기
    kboConstantsRepository.save(incoming);
  }

  private Integer parseInt(String text) {
    try {
      return Integer.parseInt(text.replaceAll("[^0-9]", ""));
    } catch (Exception e) {
      return null;
    }
  }

  private Double parseDouble(String text) {
    if (text == null || text.isEmpty() || text.equals("-") || text.equals("—"))
      return null;
    try {
      return Double.parseDouble(text.replace(",", ""));
    } catch (Exception e) {
      return null;
    }
  }
}
