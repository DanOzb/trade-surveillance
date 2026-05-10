package com.example.tradesurveillance.detection;

import com.example.tradesurveillance.alert.Alert;
import com.example.tradesurveillance.trade.Trade;
import com.example.tradesurveillance.trade.TradeRepository;

import java.util.Optional;

public interface AnomalyDetector {

    Optional<Alert> detect(Trade trade, TradeRepository tradeRepository);
}
