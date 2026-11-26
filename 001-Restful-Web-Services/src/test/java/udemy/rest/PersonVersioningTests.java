package udemy.rest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
class PersonVersioningTests {

    @Autowired MockMvc mockMvc;

    private MockHttpServletRequestBuilder person() {
        return person("/person");
    }

    // the version resolver reads the raw query string, so the parameter goes into the URL
    private MockHttpServletRequestBuilder person(String url) {
        return get(url).with(httpBasic("admin", "admin"));
    }

    @Test
    void defaultsToVersionOne() throws Exception {
        mockMvc.perform(person()).andExpect(jsonPath("$.name").value("Ivan Petrov"));
    }

    @Test
    void picksTheVersionFromAHeader() throws Exception {
        mockMvc.perform(person().header("X-API-Version", "2"))
                .andExpect(jsonPath("$.firstName").value("Ivan"));
    }

    @Test
    void picksTheVersionFromAQueryParameter() throws Exception {
        mockMvc.perform(person("/person?version=2"))
                .andExpect(jsonPath("$.lastName").value("Petrov"));
    }

    @Test
    void picksTheVersionFromTheMediaType() throws Exception {
        mockMvc.perform(
                        person().accept(
                                        MediaType.parseMediaType(
                                                "application/vnd.udemy.person+json;v=2")))
                .andExpect(jsonPath("$.firstName").value("Ivan"));
    }

    @Test
    void rejectsAnUnknownVersion() throws Exception {
        mockMvc.perform(person("/person?version=7")).andExpect(status().isBadRequest());
    }
}
