package udemy.microservices.conversion;

import feign.FeignException;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@RestController
public class CurrencyConversionController {

    private final CurrencyExchangeProxy proxy;

    public CurrencyConversionController(CurrencyExchangeProxy proxy) {
        this.proxy = proxy;
    }

    @GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion convert(
            @PathVariable @Pattern(regexp = "[A-Za-z]{3}") String from,
            @PathVariable @Pattern(regexp = "[A-Za-z]{3}") String to,
            @PathVariable @Positive @DecimalMax("1000000000") BigDecimal quantity) {
        ExchangeRate rate;
        try {
            rate = proxy.rate(from, to);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No exchange rate from " + from + " to " + to);
        } catch (FeignException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Currency exchange service failed", e);
        }
        return new CurrencyConversion(
                rate.id(),
                rate.from(),
                rate.to(),
                quantity,
                rate.conversionMultiple(),
                quantity.multiply(rate.conversionMultiple()),
                rate.environment());
    }
}
