package com.gridwar.api.dto;

import java.util.UUID;

public record GridCellDto(long id, int x, int y, UUID ownerPlayerId, String ownerNickname) {}
