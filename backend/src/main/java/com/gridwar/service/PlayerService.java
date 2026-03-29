package com.gridwar.service;

import com.gridwar.api.ApiException;
import com.gridwar.api.dto.PlayerStatusResponse;
import com.gridwar.api.dto.StartPlayerResponse;
import com.gridwar.config.GridWarProperties;
import com.gridwar.domain.Player;
import com.gridwar.repo.CellRepository;
import com.gridwar.repo.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PlayerService {

    private static final String CAPTCHA_ANSWER = "gridwar";

    private final PlayerRepository playerRepository;
    private final CellRepository cellRepository;
    private final EnergyService energyService;
    private final GridWarProperties props;
    private final SeasonService seasonService;

    public PlayerService(
            PlayerRepository playerRepository,
            CellRepository cellRepository,
            EnergyService energyService,
            GridWarProperties props,
            SeasonService seasonService) {
        this.playerRepository = playerRepository;
        this.cellRepository = cellRepository;
        this.energyService = energyService;
        this.props = props;
        this.seasonService = seasonService;
    }

    @Transactional
    public StartPlayerResponse startGuest(String nickname) {
        seasonService.endExpiredSeasonIfNeeded();
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Player p = new Player(id, nickname.trim(), props.maxEnergy(), now);
        playerRepository.save(p);
        return new StartPlayerResponse(id, p.getNickname());
    }

    @Transactional
    public PlayerStatusResponse status(UUID playerId) {
        seasonService.endExpiredSeasonIfNeeded();
        Player player = playerRepository
                .findById(playerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "player_not_found"));
        EnergyService.NextEnergyEta eta = energyService.computeNextEnergyEta(player);
        playerRepository.save(player);
        long owned = cellRepository.countByOwnerPlayerId(playerId);
        int until = props.captchaAfterActions() - player.getActionsSinceCaptcha();
        boolean needs = player.getActionsSinceCaptcha() >= props.captchaAfterActions();
        return new PlayerStatusResponse(
                player.getId(),
                player.getNickname(),
                player.getEnergy(),
                props.maxEnergy(),
                eta.secondsUntilNext(),
                player.getScore(),
                owned,
                needs,
                Math.max(0, until));
    }

    @Transactional
    public void verifyCaptcha(UUID playerId, String answer) {
        Player player = playerRepository
                .findById(playerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "player_not_found"));
        if (answer == null || !CAPTCHA_ANSWER.equalsIgnoreCase(answer.trim())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "captcha_failed");
        }
        player.setActionsSinceCaptcha(0);
        player.setCaptchaPassedAt(Instant.now());
        playerRepository.save(player);
    }
}
