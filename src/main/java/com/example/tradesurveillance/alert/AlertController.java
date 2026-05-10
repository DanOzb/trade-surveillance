package com.example.tradesurveillance.alert;

import com.example.tradesurveillance.common.AlertSummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alerts")

public class AlertController {
    private final AlertService alertService;
    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public List<AlertSummary> listAlerts() {
        return alertService.findAll();
    }

    @GetMapping("/{id}")
    public AlertSummary findById(@PathVariable Long id) {
        return alertService.findById(id);
    }
}
