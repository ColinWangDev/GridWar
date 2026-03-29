package com.gridwar.api.dto;

import java.util.List;

public record OverallLeaderboardResponse(List<LeaderboardEntryDto> entries) {}
