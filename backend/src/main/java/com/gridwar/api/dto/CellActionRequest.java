package com.gridwar.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CellActionRequest(
        @NotNull @Min(0) @Max(29) Integer x,
        @NotNull @Min(0) @Max(29) Integer y
) {}
