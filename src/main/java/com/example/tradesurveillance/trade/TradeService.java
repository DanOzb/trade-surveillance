package com.example.tradesurveillance.trade;

import com.example.tradesurveillance.alert.Alert;
import com.example.tradesurveillance.alert.AlertRepository;
import com.example.tradesurveillance.common.TradeResponse;
import com.example.tradesurveillance.common.AlertSummary;
import com.example.tradesurveillance.detection.AnomalyDetector;
import com.example.tradesurveillance.detection.PriceOutlierDetector;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TradeService {
    private final TradeRepository tradeRepository;
    private final AlertRepository alertRepository;
    private final List<AnomalyDetector> detectors;

    public TradeService(
            TradeRepository tradeRepository,
            AlertRepository alertRepository,
            List<AnomalyDetector> detectors) {
        this.tradeRepository = tradeRepository;
        this.alertRepository = alertRepository;
        this.detectors = detectors;
    }

    /**
     *
     * ingests trade, saves it, evaluates it with detectors and returns a response.
     *
     * @param  request request to be saved as {@link Trade} and evaluated.
     * @return a {@link TradeResponse} with a list of {@link AlertSummary}
     * for every detector that triggered an alert. Empty if none.
     */
    public TradeResponse ingest(TradeRequest request) {
        Trade trade = new Trade(
                null,
                request.symbol(),
                request.price(),
                request.volume(),
                request.status(),
                request.traderId(),
                request.timestamp()
        );

        this.tradeRepository.save(trade);

        List<Alert> triggered = new ArrayList<>();
        for (AnomalyDetector detector : detectors) {
            detector.detect(trade, tradeRepository).ifPresent(triggered::add);
        }

        alertRepository.saveAll(triggered);

        List<AlertSummary> summaries = triggered.stream()
                .map(a -> new AlertSummary(a.getRuleName(), a.getSeverity(), a.getReason()))
                .toList();

        return new TradeResponse(trade.getId(), summaries);
    }
}
