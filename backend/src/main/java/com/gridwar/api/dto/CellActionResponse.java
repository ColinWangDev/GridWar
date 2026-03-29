package com.gridwar.api.dto;

public record CellActionResponse(
        String outcome,
        String actionType,
        boolean success,
        int scoreDelta,
        int energyRemaining,
        boolean needsCaptcha
) {}
