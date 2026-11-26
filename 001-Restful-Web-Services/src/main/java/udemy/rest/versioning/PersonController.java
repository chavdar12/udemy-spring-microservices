package udemy.rest.versioning;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PersonController {

    public record PersonV1(String name) {}

    public record PersonV2(String firstName, String lastName) {}

    @GetMapping(path = "/person", version = "1")
    public PersonV1 personV1() {
        return new PersonV1("Ivan Petrov");
    }

    @GetMapping(path = "/person", version = "2")
    public PersonV2 personV2() {
        return new PersonV2("Ivan", "Petrov");
    }
}
