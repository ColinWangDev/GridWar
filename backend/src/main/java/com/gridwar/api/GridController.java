package com.gridwar.api;

import com.gridwar.api.dto.GridResponse;
import com.gridwar.service.GridService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GridController {

    private final GridService gridService;

    public GridController(GridService gridService) {
        this.gridService = gridService;
    }

    @GetMapping("/grid")
    public GridResponse grid() {
        return gridService.fullGrid();
    }
}
