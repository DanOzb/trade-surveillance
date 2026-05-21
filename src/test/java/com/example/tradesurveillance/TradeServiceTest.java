package com.example.tradesurveillance;

import com.example.tradesurveillance.alert.Alert;
import com.example.tradesurveillance.alert.AlertRepository;
import com.example.tradesurveillance.alert.AlertSeverity;
import com.example.tradesurveillance.common.TradeResponse;
import com.example.tradesurveillance.detection.AnomalyDetector;
import com.example.tradesurveillance.trade.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    private final TradeRepository tradeRepository = mock(TradeRepository.class);
    private final AlertRepository alertRepository = mock(AlertRepository.class);
    private final AnomalyDetector detectorA = mock(AnomalyDetector.class);
    private final AnomalyDetector detectorB = mock(AnomalyDetector.class);

    private TradeRequest sampleRequest() {
        return new TradeRequest(
                "ERIC",
                new BigDecimal("150.00"),
                100,
                TradeStatus.BUY,
                "trader-1",
                Instant.parse("2026-01-15T10:00:00Z")
        );
    }

    private Alert sampleAlert(String ruleName) {
        Alert alert = new Alert();
        alert.setRuleName(ruleName);
        alert.setSeverity(AlertSeverity.WARNING);
        alert.setReason("reason");
        alert.setTimestamp(Instant.now());
        return alert;
    }

    @Test
    void ingest_noDetectorsFire_savesTradeAndEmptyAlertList() {
        when(detectorA.detect(any(), any())).thenReturn(Optional.empty());
        when(detectorB.detect(any(), any())).thenReturn(Optional.empty());

        TradeService service = new TradeService(
                tradeRepository, alertRepository, List.of(detectorA, detectorB));

        TradeResponse response = service.ingest(sampleRequest());

        verify(tradeRepository).save(any(Trade.class));
        verify(alertRepository).saveAll(List.of());
        assertThat(response.alerts()).isEmpty();
    }

    @Test
    void ingest_multipleDetectorsConfigured_invokesAllOfThem() {
        when(detectorA.detect(any(), any())).thenReturn(Optional.empty());
        when(detectorB.detect(any(), any())).thenReturn(Optional.empty());

        TradeService service = new TradeService(
                tradeRepository, alertRepository, List.of(detectorA, detectorB));

        service.ingest(sampleRequest());

        verify(detectorA).detect(any(Trade.class), eq(tradeRepository));
        verify(detectorB).detect(any(Trade.class), eq(tradeRepository));
    }

    @Test
    void ingest_multipleDetectorsFire_persistsAllTriggeredAlerts() {
        when(detectorA.detect(any(), any())).thenReturn(Optional.of(sampleAlert("RuleA")));
        when(detectorB.detect(any(), any())).thenReturn(Optional.of(sampleAlert("RuleB")));

        TradeService service = new TradeService(
                tradeRepository, alertRepository, List.of(detectorA, detectorB));

        TradeResponse response = service.ingest(sampleRequest());

        ArgumentCaptor<List<Alert>> captor = ArgumentCaptor.forClass(List.class);
        verify(alertRepository).saveAll(captor.capture());

        assertThat(captor.getValue())
                .extracting(Alert::getRuleName)
                .containsExactly("RuleA", "RuleB");
        assertThat(response.alerts()).hasSize(2);
    }

    @Test
    void ingest_validRequest_mapsAllFieldsOntoTradeEntity() {
        when(detectorA.detect(any(), any())).thenReturn(Optional.empty());

        TradeService service = new TradeService(
                tradeRepository, alertRepository, List.of(detectorA));

        service.ingest(sampleRequest());

        ArgumentCaptor<Trade> captor = ArgumentCaptor.forClass(Trade.class);
        verify(tradeRepository).save(captor.capture());

        Trade saved = captor.getValue();
        assertThat(saved.getSymbol()).isEqualTo("ERIC");
        assertThat(saved.getPrice()).isEqualByComparingTo("150.00");
        assertThat(saved.getVolume()).isEqualTo(100);
        assertThat(saved.getStatus()).isEqualTo(TradeStatus.BUY);
        assertThat(saved.getTraderId()).isEqualTo("trader-1");
        assertThat(saved.getId()).isNull(); // not yet persisted in this layer
    }
}
