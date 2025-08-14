package com.Tiebreaker.dto.kboInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import com.Tiebreaker.entity.kboInfo.Player;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSearchResponseDto {

  private List<Player> players;
  private int totalPages;
  private long totalElements;
  private int currentPage;

}
