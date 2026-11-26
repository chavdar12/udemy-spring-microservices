package udemy.microservices.conversion;

import java.math.BigDecimal;

public record CurrencyConversion(
        Long id,
        String from,
        String to,
        BigDecimal quantity,
        BigDecimal conversionMultiple,
        BigDecimal totalCalculatedAmount,
        String environment) {}
