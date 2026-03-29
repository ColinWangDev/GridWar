package com.gridwar.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CaptchaVerifyRequest(@NotBlank String answer) {}
