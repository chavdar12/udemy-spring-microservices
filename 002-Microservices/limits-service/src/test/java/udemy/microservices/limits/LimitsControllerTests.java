package udemy.microservices.limits;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"spring.cloud.config.enabled=false", "limits-service.maximum=50"})
@AutoConfigureMockMvc
class LimitsControllerTests {

    @Autowired MockMvc mockMvc;

    @Test
    void returnsTheConfiguredLimits() throws Exception {
        mockMvc.perform(get("/limits"))
                .andExpect(jsonPath("$.minimum").value(1))
                .andExpect(jsonPath("$.maximum").value(50));
    }
}
