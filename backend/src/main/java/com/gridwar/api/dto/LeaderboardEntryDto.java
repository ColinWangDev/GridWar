package com.gridwar.api.dto;

import java.util.UUID;

public record LeaderboardEntryDto(int rank, UUID playerId, String nickname, int score, long ownedCells) {}
