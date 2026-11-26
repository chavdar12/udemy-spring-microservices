package udemy.microservices.gateway;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

// the stub echoes the path it received, so the tests see where the gateway forwarded each call
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "eureka.client.enabled=false")
@AutoConfigureWebTestClient
class ApiGatewayTests {

    static final HttpServer backend = echo();

    @Autowired WebTestClient client;

    static HttpServer echo() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext(
                    "/",
                    ex -> {
                        byte[] body = ex.getRequestURI().getPath().getBytes(StandardCharsets.UTF_8);
                        ex.sendResponseHeaders(200, body.length);
                        ex.getResponseBody().write(body);
                        ex.close();
                    });
            server.start();
            return server;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @DynamicPropertySource
    static void backends(DynamicPropertyRegistry registry) {
        String url = "http://localhost:" + backend.getAddress().getPort();
        registry.add("gateway.exchange-uri", () -> url);
        registry.add("gateway.conversion-uri", () -> url);
    }

    @AfterAll
    static void stop() {
        backend.stop(0);
    }

    private void expectForwarded(String path, String forwardedTo) {
        client.get()
                .uri(path)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo(forwardedTo);
    }

    @Test
    void routesExchangeCalls() {
        expectForwarded("/currency-exchange/from/USD/to/EUR", "/currency-exchange/from/USD/to/EUR");
    }

    @Test
    void routesConversionCalls() {
        expectForwarded(
                "/currency-conversion/from/USD/to/EUR/quantity/5",
                "/currency-conversion/from/USD/to/EUR/quantity/5");
    }

    @Test
    void rewritesTheShortConversionUrl() {
        expectForwarded(
                "/convert/from/USD/to/EUR/quantity/5",
                "/currency-conversion/from/USD/to/EUR/quantity/5");
    }

    @Test
    void unknownPathsAre404() {
        client.get().uri("/nowhere").exchange().expectStatus().isNotFound();
    }
}
