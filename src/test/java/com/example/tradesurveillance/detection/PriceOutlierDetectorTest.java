package com.example.tradesurveillance.detection;

import com.example.tradesurveillance.alert.Alert;
import com.example.tradesurveillance.alert.AlertSeverity;
import com.example.tradesurveillance.trade.Trade;
import com.example.tradesurveillance.trade.TradeRepository;
import com.example.tradesurveillance.trade.TradeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceOutlierDetectorTest {

    private static final String SYMBOL = "ERIC";

    @Mock
    private TradeRepository tradeRepository;

    private PriceOutlierDetector detector;

    @BeforeEach
    void setUp() {
        detector = new PriceOutlierDetector();
    }

    @Test
    void fewer_than_20_trades(){
        List<Trade> history = buildHistory();
        when(tradeRepository.findBySymbolOrderByTimestampDesc(eq(SYMBOL), any(Pageable.class)))
                .thenReturn(history);

        Trade incoming = buildTrade(1L, 500.0); // wildly out of band, but ignored
        Optional<Alert> result = detector.detect(incoming, tradeRepository);
        assert(result).isEmpty();
    }

    @Test
    void no_outlier_trades(){
        List<Trade> history = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            history.add(buildTrade((long) i, i % 2 == 0 ? 99.5 : 100.5));
        }
        when(tradeRepository.findBySymbolOrderByTimestampDesc(eq(SYMBOL), any(Pageable.class)))
                .thenReturn(history);

        Trade incoming = buildTrade(99L, 100.0);
        Optional<Alert> result = detector.detect(incoming, tradeRepository);
        assert(result).isEmpty();
    }

    @Test
    void clear_outlier_trade() {
        List<Trade> history = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            history.add(buildTrade((long) i, i % 2 == 0 ? 99.0 : 101.0));
        }
        when(tradeRepository.findBySymbolOrderByTimestampDesc(eq(SYMBOL), any(Pageable.class)))
                .thenReturn(history);

        Instant ts = Instant.parse("2026-01-15T10:00:00Z");
        Trade incoming = new Trade(
                42L, SYMBOL, BigDecimal.valueOf(105.0), 100,
                TradeStatus.BUY, "trader-1", ts
        );

        Optional<Alert> result = detector.detect(incoming, tradeRepository);

        assert(result).isPresent();
        Alert alert = result.get();
        assert(alert.getRuleName().equals("PriceOutlierDetector"));
        assert (alert.getSeverity().equals(AlertSeverity.WARNING));
        assert (alert.getTradeId().equals(42L));
        assert (alert.getTimestamp().equals(ts));
        assert (alert.getReason().contains("3.0"));
    }

    @Test
    void outlier_just_above_threshold() {
        BoundaryHistory h = buildBoundaryHistory();
        when(tradeRepository.findBySymbolOrderByTimestampDesc(eq(SYMBOL), any(Pageable.class)))
                .thenReturn(h.trades);

        double price = h.mean + 3.01 * h.stddev;
        Trade incoming = buildTrade(1000L, price);

        Optional<Alert> result = detector.detect(incoming, tradeRepository);

        assert (result).isPresent();
        assert (result.get().getRuleName().equals("PriceOutlierDetector"));
    }

    @Test
    void trade_within_threshold() {
        BoundaryHistory h = buildBoundaryHistory();
        when(tradeRepository.findBySymbolOrderByTimestampDesc(eq(SYMBOL), any(Pageable.class)))
                .thenReturn(h.trades);

        double price = h.mean + 2.99 * h.stddev;
        Trade incoming = buildTrade(1001L, price);

        Optional<Alert> result = detector.detect(incoming, tradeRepository);

        assert(result).isEmpty();
    }

    @Test
    void outlier_under_threshold() {
        List<Trade> history = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            history.add(buildTrade((long) i, i % 2 == 0 ? 99.0 : 101.0));
        }
        when(tradeRepository.findBySymbolOrderByTimestampDesc(eq(SYMBOL), any(Pageable.class)))
                .thenReturn(history);

        Trade incoming = buildTrade(77L, 95.0);

        Optional<Alert> result = detector.detect(incoming, tradeRepository);

        assert(result).isPresent();
        assert(result.get().getSeverity().equals(AlertSeverity.WARNING));
    }


    private BoundaryHistory buildBoundaryHistory() {
        double mean = 100.0;
        List<Trade> trades = new ArrayList<>();
        for(int i = 0; i < 10; i++) trades.add(buildTrade((long) i, mean - 1.0));
        for(int i = 10; i < 20; i++) trades.add(buildTrade((long) i, mean + 1.0));
        double variance = 20.0 / 19.0;
        double stddev = Math.sqrt(variance);
        return new BoundaryHistory(trades, mean, stddev);
    }

    private Trade buildTrade(Long id, double price) {
        return new Trade(
                id,
                SYMBOL,
                BigDecimal.valueOf(price),
                100,
                TradeStatus.BUY,
                "trader-1",
                Instant.parse("2026-01-01T00:00:00Z").plusSeconds(id == null ? 0 : id)
        );
    }

    private List<Trade> buildHistory() {
        List<Trade> trades = new ArrayList<>();
        for (int i = 0; i < 19; i++) {
            trades.add(buildTrade((long) i, 100.0));
        }
        return trades;
    }

    private record BoundaryHistory(List<Trade> trades, double mean, double stddev) {}

}