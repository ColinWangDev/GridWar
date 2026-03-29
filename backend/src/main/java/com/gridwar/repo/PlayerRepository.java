package com.gridwar.repo;

import com.gridwar.domain.Player;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {

    List<Player> findByOrderByScoreDesc(Pageable pageable);
}
