package com.example.tradesurveillance.alert;

import com.example.tradesurveillance.common.AlertSummary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AlertService {
    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public AlertSummary findById(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Alert with id " + id + " not found."
                ));
        return toSummary(alert);
    }

    public List<AlertSummary> findAll() {
        return alertRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    private AlertSummary toSummary(Alert alert) {
        return new AlertSummary(
                alert.getRuleName(),
                alert.getSeverity(),
                alert.getReason()
        );
    }

}
