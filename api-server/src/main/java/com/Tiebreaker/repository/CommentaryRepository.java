package com.Tiebreaker.repository;

import com.Tiebreaker.entity.Commentary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentaryRepository extends JpaRepository<Commentary, Long> {
  Page<Commentary> findByGameIdOrderByTsDesc(String gameId, Pageable pageable);
}
