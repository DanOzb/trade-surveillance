package com.example.tradesurveillance.common;

import java.util.List;

public record TradeResponse(
        Long tradeId,
        List<AlertSummary> alerts
) {}

