package com.gridwar.api.dto;

import java.util.List;

public record GridResponse(int size, List<GridCellDto> cells) {}
