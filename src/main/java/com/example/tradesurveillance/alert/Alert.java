package com.example.tradesurveillance.alert;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Long tradeId;

    @Column(nullable = false)
    private String ruleName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertSeverity severity;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private Instant timestamp;
}
