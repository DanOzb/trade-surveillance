package com.example.tradesurveillance;

import com.example.tradesurveillance.alert.AlertRepository;
import com.example.tradesurveillance.trade.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class PipelineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private AlertRepository alertRepository;

    @BeforeEach
    void cleanDatabase() {
        alertRepository.deleteAll();
        tradeRepository.deleteAll();
    }

    @Test
    void postTrade_outlierAfterNormalHistory_producesOneAlert() throws Exception {
        String symbol = "ACME";
        Instant base = Instant.parse("2026-01-15T09:00:00Z");

        for (int i = 0; i < 25; i++) {
            BigDecimal price = (i % 2 == 0)
                    ? new BigDecimal("99.50")
                    : new BigDecimal("100.50");
            postTrade(symbol, price, base.plusSeconds(i));
        }

        MvcResult outlierResult = postTrade(
                symbol,
                new BigDecimal("150.00"),
                base.plusSeconds(100)
        );

        objectMapper.readTree(outlierResult.getResponse().getContentAsString());

        MvcResult listResult = mockMvc.perform(get("/alerts"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode alerts = objectMapper.readTree(listResult.getResponse().getContentAsString());
        assert (alerts.isArray());
        assert(alerts.size() == 1);

        JsonNode alert = alerts.get(0);
        assert(alert.get("ruleName").asText().equals("PriceOutlierDetector"));
        assert (alert.get("severity").asText().equals("WARNING"));
        assert (alert.get("reason").asText().contains("3.0"));
    }

    @Test
    void postTrade_normalTradeAfterNormalHistory_producesNoAlerts() throws Exception {
        String symbol = "ACME";
        Instant base = Instant.parse("2026-01-15T09:00:00Z");

        for (int i = 0; i < 25; i++) {
            BigDecimal price = (i % 2 == 0)
                    ? new BigDecimal("99.50")
                    : new BigDecimal("100.50");
            postTrade(symbol, price, base.plusSeconds(i));
        }

        postTrade(symbol, new BigDecimal("100.00"), base.plusSeconds(100));

        MvcResult listResult = mockMvc.perform(get("/alerts"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode alerts = objectMapper.readTree(listResult.getResponse().getContentAsString());
        assertThat(alerts.isArray()).isTrue();
        assertThat(alerts.size()).isZero();
    }

    private MvcResult postTrade(String symbol, BigDecimal price, Instant timestamp) throws Exception {
        String body = """
                {
                  "symbol": "%s",
                  "price": %s,
                  "volume": 100,
                  "status": "BUY",
                  "traderId": "trader-1",
                  "timestamp": "%s"
                }
                """.formatted(symbol, price.toPlainString(), timestamp.toString());

        return mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
    }
}
