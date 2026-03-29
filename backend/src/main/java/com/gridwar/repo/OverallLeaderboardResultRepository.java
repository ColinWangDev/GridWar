package com.gridwar.repo;

import com.gridwar.domain.OverallLeaderboardResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OverallLeaderboardResultRepository extends JpaRepository<OverallLeaderboardResult, Long> {

    @Query(
            """
            SELECT o FROM OverallLeaderboardResult o
            WHERE o.createdAt = (SELECT MAX(o2.createdAt) FROM OverallLeaderboardResult o2)
            ORDER BY o.rank ASC
            """)
    List<OverallLeaderboardResult> findLatestSnapshot();
}
