package com.gridwar.api;

import com.gridwar.api.dto.SeasonResponse;
import com.gridwar.domain.Season;
import com.gridwar.service.LeaderboardService;
import com.gridwar.service.SeasonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/season")
public class SeasonController {

    private final SeasonService seasonService;
    private final LeaderboardService leaderboardService;

    public SeasonController(SeasonService seasonService, LeaderboardService leaderboardService) {
        this.seasonService = seasonService;
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public SeasonResponse season() {
        seasonService.endExpiredSeasonIfNeeded();
        Season s = seasonService.currentSeason();
        return new SeasonResponse(
                s.getStartedAt(), s.getEndsAt(), leaderboardService.currentSeasonTopPlayers());
    }
}
