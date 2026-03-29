package com.gridwar.repo;

import com.gridwar.domain.GameAction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameActionRepository extends JpaRepository<GameAction, Long> {}
