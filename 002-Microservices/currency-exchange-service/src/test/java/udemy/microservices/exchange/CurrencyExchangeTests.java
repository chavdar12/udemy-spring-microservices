package udemy.microservices.exchange;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
        properties = {
            "eureka.client.enabled=false",
            "resilience4j.retry.instances.sample-api.wait-duration=10ms",
            // nothing listens on port 1, so every call fails fast
            "sample-api.url=http://localhost:1/unavailable"
        })
@AutoConfigureMockMvc
class CurrencyExchangeTests {

    @Autowired MockMvc mockMvc;

    @Test
    void returnsTheExchangeRate() throws Exception {
        mockMvc.perform(get("/currency-exchange/from/usd/to/EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("USD"))
                .andExpect(jsonPath("$.to").value("EUR"))
                .andExpect(jsonPath("$.conversionMultiple").value(0.86))
                .andExpect(jsonPath("$.environment").exists());
    }

    @Test
    void unknownPairIs404() throws Exception {
        mockMvc.perform(get("/currency-exchange/from/JPY/to/EUR")).andExpect(status().isNotFound());
    }

    @Test
    void malformedCurrencyIs400() throws Exception {
        mockMvc.perform(get("/currency-exchange/from/US1/to/EUR"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void failingDownstreamFallsBack() throws Exception {
        mockMvc.perform(get("/sample-api")).andExpect(content().string("fallback-response"));
    }

    @Test
    void rateLimiterRejectsTheThirdCallInAWindow() throws Exception {
        mockMvc.perform(get("/rate-limited-api")).andExpect(status().isOk());
        mockMvc.perform(get("/rate-limited-api")).andExpect(status().isOk());
        mockMvc.perform(get("/rate-limited-api")).andExpect(status().isTooManyRequests());
    }
}
