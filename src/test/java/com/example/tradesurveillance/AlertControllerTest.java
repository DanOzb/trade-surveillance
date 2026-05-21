package com.example.tradesurveillance;

import com.example.tradesurveillance.alert.AlertController;
import com.example.tradesurveillance.alert.AlertService;
import com.example.tradesurveillance.alert.AlertSeverity;
import com.example.tradesurveillance.common.AlertSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlertService alertService;

    @Test
    void listAlerts_noAlerts_returnsEmptyArray() throws Exception {
        when(alertService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/alerts"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void listAlerts_alertsPresent_returnsAlertArray() throws Exception {
        when(alertService.findAll()).thenReturn(List.of(
                new AlertSummary("PriceOutlierDetector", AlertSeverity.WARNING, "Z score 4.2 above threshold 3.0")
        ));

        mockMvc.perform(get("/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ruleName").value("PriceOutlierDetector"))
                .andExpect(jsonPath("$[0].severity").value("WARNING"));
    }

    @Test
    void findById_alertExists_returnsAlert() throws Exception {
        when(alertService.findById(1L)).thenReturn(
                new AlertSummary("PriceOutlierDetector", AlertSeverity.WARNING, "reason")
        );

        mockMvc.perform(get("/alerts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruleName").value("PriceOutlierDetector"));
    }

    @Test
    void findById_alertMissing_returnsNotFound() throws Exception {
        when(alertService.findById(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert with id 999 not found."));

        mockMvc.perform(get("/alerts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_idNotANumber_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/alerts/abc"))
                .andExpect(status().isBadRequest());
    }
}
