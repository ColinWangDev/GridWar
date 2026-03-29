package com.gridwar.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StartPlayerRequest(
        @NotBlank @Size(min = 1, max = 64) String nickname
) {}
