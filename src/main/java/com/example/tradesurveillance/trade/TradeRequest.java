package com.example.tradesurveillance.trade;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeRequest(
        String symbol,
        BigDecimal price,
        Integer volume,
        TradeStatus status,
        String traderId,
        Instant timestamp
) {}
