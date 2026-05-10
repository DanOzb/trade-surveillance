package com.example.tradesurveillance.trade;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findBySymbolOrderByTimestampDesc(String symbol, Pageable pageable);
}
