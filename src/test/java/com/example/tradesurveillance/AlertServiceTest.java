package com.example.tradesurveillance;

import com.example.tradesurveillance.alert.Alert;
import com.example.tradesurveillance.alert.AlertRepository;
import com.example.tradesurveillance.alert.AlertService;
import com.example.tradesurveillance.alert.AlertSeverity;
import com.example.tradesurveillance.common.AlertSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    private final AlertRepository alertRepository = mock(AlertRepository.class);
    private final AlertService service = new AlertService(alertRepository);

    private Alert sampleAlert(Long id, String rule) {
        Alert a = new Alert();
        a.setId(id);
        a.setTradeId(42L);
        a.setRuleName(rule);
        a.setSeverity(AlertSeverity.WARNING);
        a.setReason("reason");
        a.setTimestamp(Instant.now());
        return a;
    }

    @Test
    void findById_alertExists_returnsSummary() {
        when(alertRepository.findById(1L))
                .thenReturn(Optional.of(sampleAlert(1L, "PriceOutlierDetector")));

        AlertSummary result = service.findById(1L);

        assertThat(result.ruleName()).isEqualTo("PriceOutlierDetector");
        assertThat(result.severity()).isEqualTo(AlertSeverity.WARNING);
    }

    @Test
    void findById_alertMissing_throwsNotFound() {
        when(alertRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("999");
    }

    @Test
    void findAll_noAlerts_returnsEmptyList() {
        when(alertRepository.findAll()).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }

    @Test
    void findAll_multipleAlerts_mapsAllToSummaries() {
        when(alertRepository.findAll()).thenReturn(List.of(
                sampleAlert(1L, "RuleA"),
                sampleAlert(2L, "RuleB")
        ));

        List<AlertSummary> result = service.findAll();

        assertThat(result)
                .extracting(AlertSummary::ruleName)
                .containsExactly("RuleA", "RuleB");
    }
}
