package com.example.tradesurveillance;

import com.example.tradesurveillance.common.TradeResponse;
import com.example.tradesurveillance.trade.TradeController;
import com.example.tradesurveillance.trade.TradeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TradeController.class)
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private TradeService tradeService;

    @Test
    void ingest_validRequest_returnsCreated() throws Exception {
        when(tradeService.ingest(any()))
                .thenReturn(new TradeResponse(1L, List.of()));

        String body = """
            {
              "symbol": "ERIC",
              "price": 150.00,
              "volume": 100,
              "status": "BUY",
              "traderId": "trader-1",
              "timestamp": "2026-01-15T10:00:00Z"
            }
            """;

        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tradeId").value(1))
                .andExpect(jsonPath("$.alerts").isArray());
    }

    @Test
    void ingest_nullPrice_returnsBadRequest() throws Exception {
        String body = """
            {
              "symbol": "ERIC",
              "price": null,
              "volume": 100,
              "status": "BUY",
              "traderId": "trader-1",
              "timestamp": "2026-01-15T10:00:00Z"
            }
            """;

        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(tradeService);
    }

    @Test
    void ingest_blankSymbol_returnsBadRequest() throws Exception {
        String body = """
            {
              "symbol": "",
              "price": 150.00,
              "volume": 100,
              "status": "BUY",
              "traderId": "trader-1",
              "timestamp": "2026-01-15T10:00:00Z"
            }
            """;

        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ingest_negativeVolume_returnsBadRequest() throws Exception {
        String body = """
            {
              "symbol": "ERIC",
              "price": 150.00,
              "volume": -5,
              "status": "BUY",
              "traderId": "trader-1",
              "timestamp": "2026-01-15T10:00:00Z"
            }
            """;

        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ingest_invalidStatus_returnsBadRequest() throws Exception {
        String body = """
            {
              "symbol": "ERIC",
              "price": 150.00,
              "volume": 100,
              "status": "HODL",
              "traderId": "trader-1",
              "timestamp": "2026-01-15T10:00:00Z"
            }
            """;

        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ingest_malformedJsonBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ingest_missingContentType_returnsUnsupportedMediaType() throws Exception {
        String body = """
            {
            "symbol":"ERIC",
            "price":150.00,
            "volume":100,
            "status":"BUY",
            "traderId":"trader-1",
            "timestamp":"2026-01-15T10:00:00Z"
            }
            """;

        mockMvc.perform(post("/trades").content(body))
                .andExpect(status().isUnsupportedMediaType());
    }
}
