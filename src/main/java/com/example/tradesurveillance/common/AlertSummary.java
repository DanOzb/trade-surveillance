package com.example.tradesurveillance.common;

import com.example.tradesurveillance.alert.AlertSeverity;

public record AlertSummary(
        String ruleName,
        AlertSeverity severity,
        String reason
) {}
