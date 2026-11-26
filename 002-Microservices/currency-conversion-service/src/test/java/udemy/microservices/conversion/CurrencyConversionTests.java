package udemy.microservices.conversion;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

// a real Feign call against a tiny stub of the currency-exchange service
@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
class CurrencyConversionTests {

    static final HttpServer exchange = stub();

    @Autowired MockMvc mockMvc;

    static HttpServer stub() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext(
                    "/currency-exchange/from/USD/to/EUR",
                    ex -> {
                        byte[] body =
                                "{\"id\":1001,\"from\":\"USD\",\"to\":\"EUR\",\"conversionMultiple\":0.86,\"environment\":\"8000\"}"
                                        .getBytes(StandardCharsets.UTF_8);
                        ex.getResponseHeaders().add("Content-Type", "application/json");
                        ex.sendResponseHeaders(200, body.length);
                        ex.getResponseBody().write(body);
                        ex.close();
                    });
            server.createContext(
                    "/currency-exchange/from/USD/to/BRL",
                    ex -> {
                        ex.sendResponseHeaders(500, -1);
                        ex.close();
                    });
            // every other path answers 404, like the real service for an unknown pair
            server.start();
            return server;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @DynamicPropertySource
    static void exchangeUrl(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.cloud.openfeign.client.config.currency-exchange.url",
                () -> "http://localhost:" + exchange.getAddress().getPort());
    }

    @AfterAll
    static void stop() {
        exchange.stop(0);
    }

    @Test
    void convertsWithTheRateFromTheExchangeService() throws Exception {
        mockMvc.perform(get("/currency-conversion/from/USD/to/EUR/quantity/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCalculatedAmount").value(8.6))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.environment").value("8000"));
    }

    @Test
    void unknownPairIs404() throws Exception {
        mockMvc.perform(get("/currency-conversion/from/USD/to/JPY/quantity/10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void failingExchangeServiceIs502() throws Exception {
        mockMvc.perform(get("/currency-conversion/from/USD/to/BRL/quantity/10"))
                .andExpect(status().isBadGateway());
    }

    @Test
    void rejectsInvalidInput() throws Exception {
        mockMvc.perform(get("/currency-conversion/from/USD/to/EUR/quantity/-5"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/currency-conversion/from/US/to/EUR/quantity/5"))
                .andExpect(status().isBadRequest());
    }
}
