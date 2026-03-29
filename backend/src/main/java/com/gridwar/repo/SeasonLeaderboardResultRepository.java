package com.gridwar.repo;

import com.gridwar.domain.SeasonLeaderboardResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface SeasonLeaderboardResultRepository extends JpaRepository<SeasonLeaderboardResult, Long> {

    List<SeasonLeaderboardResult> findBySeasonStartAndSeasonEndOrderByRankAsc(
            Instant seasonStart, Instant seasonEnd);
}
