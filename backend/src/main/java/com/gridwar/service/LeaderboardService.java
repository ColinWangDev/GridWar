package com.gridwar.service;

import com.gridwar.api.dto.LeaderboardEntryDto;
import com.gridwar.domain.OverallLeaderboardResult;
import com.gridwar.domain.Player;
import com.gridwar.repo.CellRepository;
import com.gridwar.repo.OverallLeaderboardResultRepository;
import com.gridwar.repo.PlayerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeaderboardService {

    private static final int TOP = 20;

    private final PlayerRepository playerRepository;
    private final CellRepository cellRepository;
    private final OverallLeaderboardResultRepository overallLeaderboardResultRepository;

    public LeaderboardService(
            PlayerRepository playerRepository,
            CellRepository cellRepository,
            OverallLeaderboardResultRepository overallLeaderboardResultRepository) {
        this.playerRepository = playerRepository;
        this.cellRepository = cellRepository;
        this.overallLeaderboardResultRepository = overallLeaderboardResultRepository;
    }

    public List<LeaderboardEntryDto> currentSeasonTopPlayers() {
        List<Player> players = playerRepository.findByOrderByScoreDesc(PageRequest.of(0, TOP));
        return toEntries(players);
    }

    public List<LeaderboardEntryDto> overallLatestSnapshot() {
        List<OverallLeaderboardResult> rows = overallLeaderboardResultRepository.findLatestSnapshot();
        if (rows.isEmpty()) {
            return List.of();
        }
        List<LeaderboardEntryDto> out = new ArrayList<>();
        for (OverallLeaderboardResult r : rows) {
            if (r.getRank() > TOP) {
                break;
            }
            out.add(new LeaderboardEntryDto(
                    r.getRank(), r.getPlayerId(), r.getPlayerNickname(), r.getScore(), 0L));
        }
        return out;
    }

    private List<LeaderboardEntryDto> toEntries(List<Player> players) {
        List<LeaderboardEntryDto> out = new ArrayList<>();
        int rank = 1;
        for (Player p : players) {
            long owned = cellRepository.countByOwnerPlayerId(p.getId());
            out.add(new LeaderboardEntryDto(rank++, p.getId(), p.getNickname(), p.getScore(), owned));
        }
        return out;
    }
}
