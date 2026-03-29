package com.gridwar.service;

import com.gridwar.config.GridWarProperties;
import com.gridwar.domain.Player;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class EnergyService {

    private final GridWarProperties props;

    public EnergyService(GridWarProperties props) {
        this.props = props;
    }

    public void applyRegeneration(Player player) {
        Instant now = Instant.now();
        Instant last = player.getLastEnergyUpdate();
        long regenMinutes = props.energyRegenMinutes();
        Duration elapsed = Duration.between(last, now);
        if (elapsed.toMinutes() < regenMinutes) {
            return;
        }
        long slots = elapsed.toMinutes() / regenMinutes;
        if (slots <= 0) {
            return;
        }
        int max = props.maxEnergy();
        int newEnergy = Math.min(max, player.getEnergy() + (int) slots);
        player.setEnergy(newEnergy);
        player.setLastEnergyUpdate(last.plus(slots * regenMinutes, ChronoUnit.MINUTES));
    }

    public NextEnergyEta computeNextEnergyEta(Player player) {
        applyRegeneration(player);
        Instant now = Instant.now();
        if (player.getEnergy() >= props.maxEnergy()) {
            return new NextEnergyEta(0, null);
        }
        Instant nextAt = player.getLastEnergyUpdate().plus(props.energyRegenMinutes(), ChronoUnit.MINUTES);
        long seconds = Math.max(0, Duration.between(now, nextAt).getSeconds());
        return new NextEnergyEta(seconds, nextAt);
    }

    public record NextEnergyEta(long secondsUntilNext, Instant nextEnergyAt) {}
}
