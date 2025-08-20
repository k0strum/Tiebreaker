package com.Tiebreaker.repository.kboInfo;

import com.Tiebreaker.entity.kboInfo.KboConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KboConstantsRepository extends JpaRepository<KboConstants, Integer> {
  Optional<KboConstants> findByYear(int year);
}
