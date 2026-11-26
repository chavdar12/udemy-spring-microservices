package udemy.microservices.exchange;

import jakarta.validation.constraints.Pattern;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@RestController
public class CurrencyExchangeController {

    private final CurrencyExchangeRepository repository;
    private final Environment environment;

    public CurrencyExchangeController(
            CurrencyExchangeRepository repository, Environment environment) {
        this.repository = repository;
        this.environment = environment;
    }

    @GetMapping("/currency-exchange/from/{from}/to/{to}")
    public CurrencyExchange exchange(
            @PathVariable @Pattern(regexp = "[A-Za-z]{3}") String from,
            @PathVariable @Pattern(regexp = "[A-Za-z]{3}") String to) {
        CurrencyExchange exchange =
                repository
                        .findByFromAndTo(from.toUpperCase(Locale.ROOT), to.toUpperCase(Locale.ROOT))
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "No exchange rate from " + from + " to " + to));
        exchange.setEnvironment(environment.getProperty("local.server.port", "unknown"));
        return exchange;
    }
}
