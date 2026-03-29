package com.gridwar.api;

import com.gridwar.api.dto.OverallLeaderboardResponse;
import com.gridwar.service.LeaderboardService;
import com.gridwar.service.SeasonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final SeasonService seasonService;

    public LeaderboardController(LeaderboardService leaderboardService, SeasonService seasonService) {
        this.leaderboardService = leaderboardService;
        this.seasonService = seasonService;
    }

    @GetMapping
    public OverallLeaderboardResponse overall() {
        seasonService.endExpiredSeasonIfNeeded();
        return new OverallLeaderboardResponse(leaderboardService.overallLatestSnapshot());
    }
}
