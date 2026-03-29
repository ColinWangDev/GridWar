package com.gridwar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "season_leaderboard_results")
public class SeasonLeaderboardResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Column(name = "player_nickname", nullable = false, length = 64)
    private String playerNickname;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int rank;

    @Column(name = "season_start", nullable = false)
    private Instant seasonStart;

    @Column(name = "season_end", nullable = false)
    private Instant seasonEnd;

    protected SeasonLeaderboardResult() {}

    public SeasonLeaderboardResult(
            UUID playerId, String playerNickname, int score, int rank, Instant seasonStart, Instant seasonEnd) {
        this.playerId = playerId;
        this.playerNickname = playerNickname;
        this.score = score;
        this.rank = rank;
        this.seasonStart = seasonStart;
        this.seasonEnd = seasonEnd;
    }

    public String getPlayerNickname() {
        return playerNickname;
    }

    public int getScore() {
        return score;
    }

    public int getRank() {
        return rank;
    }

    public Instant getSeasonStart() {
        return seasonStart;
    }

    public Instant getSeasonEnd() {
        return seasonEnd;
    }
}
