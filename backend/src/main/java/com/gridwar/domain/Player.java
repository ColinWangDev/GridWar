package com.gridwar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "players")
public class Player {

    @Id
    private UUID id;

    @Column(nullable = false, length = 64)
    private String nickname;

    @Column(nullable = false)
    private int energy;

    @Column(name = "last_energy_update", nullable = false)
    private Instant lastEnergyUpdate;

    @Column(nullable = false)
    private int score;

    @Column(name = "last_action_at")
    private Instant lastActionAt;

    @Column(name = "actions_since_captcha", nullable = false)
    private int actionsSinceCaptcha;

    @Column(name = "captcha_passed_at")
    private Instant captchaPassedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Player() {}

    public Player(UUID id, String nickname, int energy, Instant lastEnergyUpdate) {
        this.id = id;
        this.nickname = nickname;
        this.energy = energy;
        this.lastEnergyUpdate = lastEnergyUpdate;
        this.score = 0;
        this.actionsSinceCaptcha = 0;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public Instant getLastEnergyUpdate() {
        return lastEnergyUpdate;
    }

    public void setLastEnergyUpdate(Instant lastEnergyUpdate) {
        this.lastEnergyUpdate = lastEnergyUpdate;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Instant getLastActionAt() {
        return lastActionAt;
    }

    public void setLastActionAt(Instant lastActionAt) {
        this.lastActionAt = lastActionAt;
    }

    public int getActionsSinceCaptcha() {
        return actionsSinceCaptcha;
    }

    public void setActionsSinceCaptcha(int actionsSinceCaptcha) {
        this.actionsSinceCaptcha = actionsSinceCaptcha;
    }

    public Instant getCaptchaPassedAt() {
        return captchaPassedAt;
    }

    public void setCaptchaPassedAt(Instant captchaPassedAt) {
        this.captchaPassedAt = captchaPassedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
