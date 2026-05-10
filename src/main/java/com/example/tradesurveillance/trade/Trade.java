package com.example.tradesurveillance.trade;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trades")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private int volume; //number of shares

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TradeStatus status; //buy or sell

    @Column(nullable = false)
    private String traderId;

    @Column(nullable = false)
    private Instant timestamp;
}
