package com.gridwar.api.dto;

import java.util.UUID;

public record PlayerStatusResponse(
        UUID playerId,
        String nickname,
        int energy,
        int maxEnergy,
        long secondsUntilNextEnergy,
        int score,
        long ownedCells,
        boolean needsCaptcha,
        int actionsUntilCaptcha
) {}
