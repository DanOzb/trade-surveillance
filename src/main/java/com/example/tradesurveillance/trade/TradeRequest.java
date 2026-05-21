package com.example.tradesurveillance.trade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeRequest(
        @NotBlank
        String symbol,
        @NotNull @Positive
        BigDecimal price,
        @NotNull @Positive
        int volume,
        @NotNull
        TradeStatus status,
        @NotEmpty
        String traderId,
        @NotNull
        Instant timestamp
) {}
