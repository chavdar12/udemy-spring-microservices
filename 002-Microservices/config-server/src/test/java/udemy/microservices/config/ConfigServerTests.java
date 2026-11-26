package udemy.microservices.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

// Gradle runs tests from the subproject folder, so the default ../config-repo resolves
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConfigServerTests {

    @LocalServerPort int port;

    private String config(String path) {
        return RestClient.create("http://localhost:" + port)
                .get()
                .uri(path)
                .retrieve()
                .body(String.class);
    }

    @Test
    void servesTheDefaultLimits() {
        assertThat(config("/limits-service/default"))
                .contains("\"limits-service.minimum\":4", "\"limits-service.maximum\":996");
    }

    @Test
    void profileFilesOverrideTheDefaults() {
        assertThat(config("/limits-service/qa")).contains("\"limits-service.maximum\":222");
    }
}
