package com.gridwar.service;

import com.gridwar.config.GridWarProperties;
import com.gridwar.domain.Player;
import com.gridwar.domain.Season;
import com.gridwar.domain.SeasonLeaderboardResult;
import com.gridwar.repo.CellRepository;
import com.gridwar.repo.PlayerRepository;
import com.gridwar.repo.SeasonLeaderboardResultRepository;
import com.gridwar.repo.SeasonRepository;
import com.gridwar.time.SeasonSchedule;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;

@Service
public class SeasonService {

    private final SeasonRepository seasonRepository;
    private final SeasonLeaderboardResultRepository seasonLeaderboardResultRepository;
    private final PlayerRepository playerRepository;
    private final CellRepository cellRepository;
    private final GridWarProperties props;
    private final TransactionTemplate transactionTemplate;

    public SeasonService(
            SeasonRepository seasonRepository,
            SeasonLeaderboardResultRepository seasonLeaderboardResultRepository,
            PlayerRepository playerRepository,
            CellRepository cellRepository,
            GridWarProperties props,
            PlatformTransactionManager transactionManager) {
        this.seasonRepository = seasonRepository;
        this.seasonLeaderboardResultRepository = seasonLeaderboardResultRepository;
        this.playerRepository = playerRepository;
        this.cellRepository = cellRepository;
        this.props = props;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public Season currentSeason() {
        return seasonRepository
                .findTopByOrderByIdDesc()
                .orElseThrow(() -> new IllegalStateException("No season configured"));
    }

    @Scheduled(fixedDelay = 60_000)
    public void tickSeasonRollover() {
        endExpiredSeasonIfNeeded();
    }

    public void endExpiredSeasonIfNeeded() {
        transactionTemplate.executeWithoutResult(status -> runAllDueRollovers());
    }

    /** Normalizes the latest row to Sydney Monday 00:00 weekly windows (e.g. after Flyway seed). */
    public void alignCurrentSeasonWindow() {
        transactionTemplate.executeWithoutResult(status -> alignLatestSeason());
    }

    private void alignLatestSeason() {
        Season latest = seasonRepository.findTopByOrderByIdDesc().orElse(null);
        Instant now = Instant.now();
        if (latest != null && !now.isBefore(latest.getEndsAt())) {
            return;
        }
        Instant properEnd = SeasonSchedule.nextMondayMidnightAfter(now);
        Instant properStart = SeasonSchedule.weekStartsAt(properEnd);
        if (latest == null) {
            seasonRepository.save(new Season(properStart, properEnd));
            return;
        }
        if (!latest.getEndsAt().equals(properEnd) || !latest.getStartedAt().equals(properStart)) {
            latest.setStartedAt(properStart);
            latest.setEndsAt(properEnd);
            seasonRepository.save(latest);
        }
    }

    private void runAllDueRollovers() {
        while (true) {
            Season season = seasonRepository.findTopByOrderByIdDesc().orElse(null);
            if (season == null) {
                return;
            }
            Instant now = Instant.now();
            if (now.isBefore(season.getEndsAt())) {
                return;
            }
            Instant seasonStart = season.getStartedAt();
            Instant seasonEnd = season.getEndsAt();
            List<Player> ranked = playerRepository.findByOrderByScoreDesc(PageRequest.of(0, 500));
            int rank = 1;
            for (Player p : ranked) {
                if (p.getScore() <= 0) {
                    continue;
                }
                seasonLeaderboardResultRepository.save(
                        new SeasonLeaderboardResult(
                                p.getId(), p.getNickname(), p.getScore(), rank++, seasonStart, seasonEnd));
            }
            cellRepository.clearAllOwnership();
            int maxEnergy = props.maxEnergy();
            Instant resetAt = Instant.now();
            List<Player> allPlayers = playerRepository.findAll();
            for (Player p : allPlayers) {
                p.setScore(0);
                p.setEnergy(maxEnergy);
                p.setLastEnergyUpdate(resetAt);
                p.setLastActionAt(null);
                p.setActionsSinceCaptcha(0);
            }
            playerRepository.saveAll(allPlayers);
            Instant newStart = seasonEnd;
            Instant newEnd =
                    seasonEnd.atZone(SeasonSchedule.SYDNEY).plusWeeks(1).toInstant();
            seasonRepository.save(new Season(newStart, newEnd));
        }
    }
}
