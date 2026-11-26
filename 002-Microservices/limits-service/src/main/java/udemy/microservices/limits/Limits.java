package udemy.microservices.limits;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("limits-service")
public record Limits(int minimum, int maximum) {}
