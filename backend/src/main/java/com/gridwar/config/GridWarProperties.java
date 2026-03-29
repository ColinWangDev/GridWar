package com.gridwar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gridwar")
public record GridWarProperties(
        int gridSize,
        int maxEnergy,
        int energyRegenMinutes,
        long actionCooldownMs,
        int captchaAfterActions
) {}
