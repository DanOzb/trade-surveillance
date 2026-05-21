package com.example.tradesurveillance.detection;

import com.example.tradesurveillance.alert.Alert;
import com.example.tradesurveillance.alert.AlertSeverity;
import com.example.tradesurveillance.trade.Trade;
import com.example.tradesurveillance.trade.TradeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class PriceOutlierDetector implements AnomalyDetector{

    private static final int WINDOW_SIZE = 100;
    private static final int MIN_SAMPLES = 20;
    private static final double Z_THRESHOLD = 3.0;

    @Override
    public Optional<Alert> detect(Trade trade, TradeRepository history) {
        List<Trade> recent = history.findBySymbolAndIdNotOrderByTimestampDesc(
                trade.getSymbol(),
                trade.getId(),
                PageRequest.of(0, WINDOW_SIZE)
        );

        if (recent.size() < MIN_SAMPLES) {
            return Optional.empty();
        }

        double[] prices = recent.stream()
                .mapToDouble(t -> t.getPrice().doubleValue())
                .toArray();

        double mean = Arrays.stream(prices).average().orElse(0);
        double variance = Arrays.stream(prices)
                .map(p -> (p - mean) * (p - mean))
                .sum() / (prices.length - 1);
        double stdDev = Math.sqrt(variance);

        if (stdDev == 0) {
            return Optional.empty();
        }

        double z = (trade.getPrice().doubleValue() - mean) / stdDev;

        if (Math.abs(z) > Z_THRESHOLD) {
            Alert alert = new Alert();
            alert.setTradeId(trade.getId());
            alert.setTimestamp(trade.getTimestamp());
            alert.setReason("Z score " + z + " is above threshold " + Z_THRESHOLD);
            alert.setSeverity(AlertSeverity.WARNING);
            alert.setRuleName("PriceOutlierDetector");
            return  Optional.of(alert);
        }

        return Optional.empty();
    }
}
