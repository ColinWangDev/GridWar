package com.gridwar.repo;

import com.gridwar.domain.Season;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, Long> {

    Optional<Season> findTopByOrderByIdDesc();
}
