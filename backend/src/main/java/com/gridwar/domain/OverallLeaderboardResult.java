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
@Table(name = "overall_leaderboard_results")
public class OverallLeaderboardResult {

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

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OverallLeaderboardResult() {}

    public UUID getPlayerId() {
        return playerId;
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
