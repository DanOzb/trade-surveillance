package com.example.tradesurveillance;

import com.example.tradesurveillance.alert.AlertRepository;
import com.example.tradesurveillance.common.TradeResponse;
import com.example.tradesurveillance.trade.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SpringTradeServiceTest {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private AlertRepository alertRepository;

    @BeforeEach
    void setUp() {
        alertRepository.deleteAll();
        tradeRepository.deleteAll();
    }

    @Test
    void ingest_priceIsOutlier_returnsAlertInResponse() {
        for (int i = 0; i < 20; i++) {
            tradeRepository.save(normalTrade("ERIC", new BigDecimal("150.00")));
        }

        TradeRequest outlierRequest = new TradeRequest(
                "ERIC",
                new BigDecimal("9999.00"),
                100,
                TradeStatus.BUY,
                "trader-1",
                Instant.now()
        );
        TradeResponse response = tradeService.ingest(outlierRequest);

        assertThat(response.alerts())
                .isNotEmpty();
        assertThat(response.alerts().getFirst().ruleName())
                .isEqualTo("PriceOutlierDetector");
    }

    @Test
    void ingest_insufficientHistory_returnsNoAlerts() {
        for (int i = 0; i < 5; i++) {
            tradeRepository.save(normalTrade("ERIC", new BigDecimal("150.00")));
        }

        TradeRequest outlierRequest = new TradeRequest(
                "ERIC",
                new BigDecimal("9999.00"),
                100,
                TradeStatus.BUY,
                "trader-1",
                Instant.now()
        );
        TradeResponse response = tradeService.ingest(outlierRequest);

        assertThat(response.alerts()).isEmpty();
    }

    private Trade normalTrade(String symbol, BigDecimal price) {
        Trade trade = new Trade();
        trade.setSymbol(symbol);
        trade.setPrice(price);
        trade.setVolume(100);
        trade.setStatus(TradeStatus.BUY);
        trade.setTraderId("trader-seed");
        trade.setTimestamp(Instant.now());
        return trade;
    }
}