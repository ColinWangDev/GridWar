package com.gridwar.repo;

import com.gridwar.domain.Cell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CellRepository extends JpaRepository<Cell, Long> {

    Optional<Cell> findByXAndY(int x, int y);

    long countByOwnerPlayerId(UUID ownerPlayerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Cell c SET c.ownerPlayerId = NULL, c.ownerNickname = NULL")
    int clearAllOwnership();
}
