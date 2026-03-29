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
@Table(name = "actions")
public class GameAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Column(name = "cell_id", nullable = false)
    private Long cellId;

    @Column(name = "action_type", nullable = false, length = 16)
    private String actionType;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected GameAction() {}

    public GameAction(UUID playerId, Long cellId, String actionType, boolean success) {
        this.playerId = playerId;
        this.cellId = cellId;
        this.actionType = actionType;
        this.success = success;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }
}
