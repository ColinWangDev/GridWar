package com.gridwar.api;

import com.gridwar.api.dto.CaptchaVerifyRequest;
import com.gridwar.api.dto.PlayerStatusResponse;
import com.gridwar.api.dto.StartPlayerRequest;
import com.gridwar.api.dto.StartPlayerResponse;
import com.gridwar.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/player")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/start")
    public StartPlayerResponse start(@Valid @RequestBody StartPlayerRequest body) {
        return playerService.startGuest(body.nickname());
    }

    @GetMapping("/status")
    public PlayerStatusResponse status(@RequestHeader("X-Player-Id") UUID playerId) {
        return playerService.status(playerId);
    }

    @PostMapping("/captcha-verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void captcha(
            @RequestHeader("X-Player-Id") UUID playerId, @Valid @RequestBody CaptchaVerifyRequest body) {
        playerService.verifyCaptcha(playerId, body.answer());
    }
}
