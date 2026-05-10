package com.example.tradesurveillance.trade;

import com.example.tradesurveillance.common.TradeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trades")

public class TradeController {
    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    public ResponseEntity<TradeResponse> ingest(@RequestBody TradeRequest request) {
        var alerts = tradeService.ingest(request);
        return ResponseEntity.status(201).body(alerts);
    }
}
