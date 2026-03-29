package com.gridwar.service;

import com.gridwar.api.ApiException;
import com.gridwar.api.dto.CellActionResponse;
import com.gridwar.config.GridWarProperties;
import com.gridwar.domain.Cell;
import com.gridwar.domain.GameAction;
import com.gridwar.domain.Player;
import com.gridwar.repo.CellRepository;
import com.gridwar.repo.GameActionRepository;
import com.gridwar.repo.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CellActionService {

    private final PlayerRepository playerRepository;
    private final CellRepository cellRepository;
    private final GameActionRepository gameActionRepository;
    private final EnergyService energyService;
    private final GridWarProperties props;
    private final SeasonService seasonService;

    public CellActionService(
            PlayerRepository playerRepository,
            CellRepository cellRepository,
            GameActionRepository gameActionRepository,
            EnergyService energyService,
            GridWarProperties props,
            SeasonService seasonService) {
        this.playerRepository = playerRepository;
        this.cellRepository = cellRepository;
        this.gameActionRepository = gameActionRepository;
        this.energyService = energyService;
        this.props = props;
        this.seasonService = seasonService;
    }

    @Transactional
    public CellActionResponse performAction(UUID playerId, int x, int y) {
        seasonService.endExpiredSeasonIfNeeded();
        int maxCoord = props.gridSize() - 1;
        if (x < 0 || x > maxCoord || y < 0 || y > maxCoord) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "invalid_cell");
        }
        Player player = playerRepository
                .findById(playerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "player_not_found"));
        energyService.applyRegeneration(player);
        if (player.getActionsSinceCaptcha() >= props.captchaAfterActions()) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "needs_captcha");
        }
        Instant now = Instant.now();
        if (player.getLastActionAt() != null) {
            long elapsed = now.toEpochMilli() - player.getLastActionAt().toEpochMilli();
            if (elapsed < props.actionCooldownMs()) {
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "cooldown");
            }
        }
        if (player.getEnergy() <= 0) {
            throw new ApiException(HttpStatus.CONFLICT, "no_energy");
        }
        Cell cell = cellRepository
                .findByXAndY(x, y)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "cell_not_found"));
        if (Objects.equals(cell.getOwnerPlayerId(), playerId)) {
            throw new ApiException(HttpStatus.CONFLICT, "already_owned");
        }
        boolean needsCaptchaAfter = player.getActionsSinceCaptcha() + 1 >= props.captchaAfterActions();
        if (cell.getOwnerPlayerId() == null) {
            return capture(player, cell, needsCaptchaAfter);
        }
        return attack(player, cell, needsCaptchaAfter);
    }

    private CellActionResponse capture(Player player, Cell cell, boolean needsCaptchaAfter) {
        cell.setOwnerPlayerId(player.getId());
        cell.setOwnerNickname(player.getNickname());
        player.setScore(player.getScore() + 10);
        spendEnergyAndTouch(player);
        gameActionRepository.save(new GameAction(player.getId(), cell.getId(), "capture", true));
        return new CellActionResponse("captured", "capture", true, 10, player.getEnergy(), needsCaptchaAfter);
    }

    private CellActionResponse attack(Player player, Cell cell, boolean needsCaptchaAfter) {
        boolean success = ThreadLocalRandom.current().nextBoolean();
        int scoreDelta;
        if (success) {
            cell.setOwnerPlayerId(player.getId());
            cell.setOwnerNickname(player.getNickname());
            scoreDelta = 20;
        } else {
            scoreDelta = 2;
        }
        player.setScore(player.getScore() + scoreDelta);
        spendEnergyAndTouch(player);
        gameActionRepository.save(new GameAction(player.getId(), cell.getId(), "attack", success));
        String outcome = success ? "attack_won" : "attack_lost";
        return new CellActionResponse(outcome, "attack", success, scoreDelta, player.getEnergy(), needsCaptchaAfter);
    }

    private void spendEnergyAndTouch(Player player) {
        player.setEnergy(player.getEnergy() - 1);
        player.setLastActionAt(Instant.now());
        player.setActionsSinceCaptcha(player.getActionsSinceCaptcha() + 1);
        playerRepository.save(player);
    }
}
