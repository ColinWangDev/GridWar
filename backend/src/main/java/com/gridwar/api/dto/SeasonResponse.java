package com.gridwar.api.dto;

import java.time.Instant;
import java.util.List;

public record SeasonResponse(Instant seasonStartedAt, Instant seasonEndsAt, List<LeaderboardEntryDto> leaderboard) {}
