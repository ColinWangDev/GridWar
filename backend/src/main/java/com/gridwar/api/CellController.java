package com.gridwar.api;

import com.gridwar.api.dto.CellActionRequest;
import com.gridwar.api.dto.CellActionResponse;
import com.gridwar.service.CellActionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/cell")
public class CellController {

    private final CellActionService cellActionService;

    public CellController(CellActionService cellActionService) {
        this.cellActionService = cellActionService;
    }

    @PostMapping("/action")
    public CellActionResponse action(
            @RequestHeader("X-Player-Id") UUID playerId, @Valid @RequestBody CellActionRequest body) {
        return cellActionService.performAction(playerId, body.x(), body.y());
    }
}
