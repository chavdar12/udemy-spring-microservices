package udemy.microservices.conversion;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// no url: Feign asks Eureka for a currency-exchange instance and load-balances across them
@FeignClient(name = "currency-exchange")
public interface CurrencyExchangeProxy {

    @GetMapping("/currency-exchange/from/{from}/to/{to}")
    ExchangeRate rate(@PathVariable String from, @PathVariable String to);
}
