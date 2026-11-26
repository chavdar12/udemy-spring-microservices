package udemy.microservices.naming;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NamingServerTests {

    @LocalServerPort int port;

    @Test
    void servesAnEmptyRegistry() {
        String apps =
                RestClient.create("http://localhost:" + port)
                        .get()
                        .uri("/eureka/apps")
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(String.class);
        assertThat(apps).contains("\"applications\"");
    }
}
