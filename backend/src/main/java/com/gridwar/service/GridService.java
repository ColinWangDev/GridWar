package com.gridwar.service;

import com.gridwar.api.dto.GridCellDto;
import com.gridwar.api.dto.GridResponse;
import com.gridwar.config.GridWarProperties;
import com.gridwar.domain.Cell;
import com.gridwar.repo.CellRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GridService {

    private final CellRepository cellRepository;
    private final GridWarProperties props;
    private final SeasonService seasonService;

    public GridService(CellRepository cellRepository, GridWarProperties props, SeasonService seasonService) {
        this.cellRepository = cellRepository;
        this.props = props;
        this.seasonService = seasonService;
    }

    @Transactional
    public GridResponse fullGrid() {
        seasonService.endExpiredSeasonIfNeeded();
        List<Cell> cells = cellRepository.findAll();
        List<GridCellDto> dtos = cells.stream()
                .map(c -> new GridCellDto(c.getId(), c.getX(), c.getY(), c.getOwnerPlayerId(), c.getOwnerNickname()))
                .toList();
        return new GridResponse(props.gridSize(), dtos);
    }
}
