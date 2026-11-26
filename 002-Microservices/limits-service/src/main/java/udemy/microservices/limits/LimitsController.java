package udemy.microservices.limits;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LimitsController {

    private final Limits limits;

    public LimitsController(Limits limits) {
        this.limits = limits;
    }

    @GetMapping("/limits")
    public Limits limits() {
        return limits;
    }
}
