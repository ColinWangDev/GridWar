package com.gridwar.api.dto;

import java.util.UUID;

public record StartPlayerResponse(UUID playerId, String nickname) {}
