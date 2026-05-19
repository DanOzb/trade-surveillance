package com.example.tradesurveillance.trade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeRequest(
        @NotBlank
        String symbol,
        @NotEmpty @Positive
        BigDecimal price,
        @NotEmpty @Positive
        int volume,
        @NotEmpty
        TradeStatus status,
        @NotEmpty
        String traderId,
        @NotEmpty
        Instant timestamp
) {}
