package udemy.microservices.exchange;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

// calls a service that is never up, to show retries, the circuit breaker and rate limiting
@RestController
public class SampleApiController {

    private final RestClient client;

    public SampleApiController(
            @Value("${sample-api.url:http://localhost:8080/unavailable}") String url) {
        this.client = RestClient.create(url);
    }

    @GetMapping("/sample-api")
    @Retry(name = "sample-api", fallbackMethod = "fallback")
    @CircuitBreaker(name = "sample-api", fallbackMethod = "fallback")
    public String sampleApi() {
        return client.get().retrieve().body(String.class);
    }

    @GetMapping("/rate-limited-api")
    @RateLimiter(name = "sample-api")
    public String rateLimitedApi() {
        return "sample-api";
    }

    String fallback(Exception e) {
        return "fallback-response";
    }
}
