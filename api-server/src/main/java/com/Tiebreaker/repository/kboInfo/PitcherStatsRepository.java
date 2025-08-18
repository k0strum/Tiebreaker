package com.Tiebreaker.repository.kboInfo;

import com.Tiebreaker.entity.kboInfo.PitcherStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PitcherStatsRepository extends JpaRepository<PitcherStats, Long> {

        Optional<PitcherStats> findByPlayerId(Long playerId);

        // 연도별 조회
        Optional<PitcherStats> findByPlayerIdAndYear(Long playerId, Integer year);

        // 승수 순위
        @Query("SELECT ps FROM PitcherStats ps " +
                        "JOIN ps.player p " +
                        "WHERE ps.wins > 0 " +
                        "ORDER BY ps.wins DESC")
        List<PitcherStats> findWinsRanking();

        // 세이브 순위
        @Query("SELECT ps FROM PitcherStats ps " +
                        "JOIN ps.player p " +
                        "WHERE ps.saves > 0 " +
                        "ORDER BY ps.saves DESC")
        List<PitcherStats> findSavesRanking();

        // 홀드 순위
        @Query("SELECT ps FROM PitcherStats ps " +
                        "JOIN ps.player p " +
                        "WHERE ps.holds > 0 " +
                        "ORDER BY ps.holds DESC")
        List<PitcherStats> findHoldsRanking();

        // 탈삼진 순위
        @Query("SELECT ps FROM PitcherStats ps " +
                        "JOIN ps.player p " +
                        "WHERE ps.strikeouts > 0 " +
                        "ORDER BY ps.strikeouts DESC")
        List<PitcherStats> findStrikeoutsRanking();

        // 방어율 순위 (계산된 값 사용, 낮을수록 좋음)
        @Query("SELECT ps FROM PitcherStats ps " +
                        "JOIN ps.player p " +
                        "WHERE ps.earnedRunAverage > 0 " +
                        "ORDER BY ps.earnedRunAverage ASC")
        List<PitcherStats> findEraRanking();

        // WHIP 순위 (계산된 값 사용, 낮을수록 좋음)
        @Query("SELECT ps FROM PitcherStats ps " +
                        "JOIN ps.player p " +
                        "WHERE ps.whip > 0 " +
                        "ORDER BY ps.whip ASC")
        List<PitcherStats> findWhipRanking();
}
