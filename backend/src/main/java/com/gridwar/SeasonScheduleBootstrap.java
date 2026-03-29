package com.gridwar;

import com.gridwar.service.SeasonService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SeasonScheduleBootstrap implements ApplicationRunner {

    private final SeasonService seasonService;

    public SeasonScheduleBootstrap(SeasonService seasonService) {
        this.seasonService = seasonService;
    }

    @Override
    public void run(ApplicationArguments args) {
        seasonService.endExpiredSeasonIfNeeded();
        seasonService.alignCurrentSeasonWindow();
    }
}
