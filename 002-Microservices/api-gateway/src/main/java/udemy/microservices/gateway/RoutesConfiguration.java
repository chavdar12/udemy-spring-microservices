package udemy.microservices.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutesConfiguration {

    @Bean
    RouteLocator routes(
            RouteLocatorBuilder builder,
            @Value("${gateway.exchange-uri}") String exchange,
            @Value("${gateway.conversion-uri}") String conversion) {
        return builder.routes()
                .route("exchange", r -> r.path("/currency-exchange/**").uri(exchange))
                .route("conversion", r -> r.path("/currency-conversion/**").uri(conversion))
                // a shorter public URL for the same conversion endpoint
                .route(
                        "conversion-short",
                        r ->
                                r.path("/convert/**")
                                        .filters(f ->
                                                f.rewritePath(
                                                        "/convert/(?<rest>.*)",
                                                        "/currency-conversion/${rest}"))
                                        .uri(conversion))
                .build();
    }
}
