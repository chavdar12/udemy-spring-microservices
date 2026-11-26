package udemy.microservices.conversion;

import java.math.BigDecimal;

public record ExchangeRate(
        Long id, String from, String to, BigDecimal conversionMultiple, String environment) {}
