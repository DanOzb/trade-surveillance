package com.example.tradesurveillance.detection;

import com.example.tradesurveillance.alert.Alert;
import com.example.tradesurveillance.trade.Trade;
import com.example.tradesurveillance.trade.TradeRepository;

import java.util.Optional;

/**
 * Detects anomalies in incoming trades against historical context.
 * <p>
 * Add {@link org.springframework.stereotype.Component} for
 * detector implementations.
 */

public interface AnomalyDetector {


    /**
     * Evaluates a single trade against historical data.
     *
     * @param trade           trade to be evaluated. Already saved.
     * @param tradeRepository repository to look up past trades.
     * @return                an {@link Alert} if the rule fires, otherwise {@link Optional#empty()}
     */
    Optional<Alert> detect(Trade trade, TradeRepository tradeRepository);
}
