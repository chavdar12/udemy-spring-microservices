package udemy.rest;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTests {

    private static final RequestPostProcessor ADMIN = httpBasic("admin", "admin");

    @Autowired MockMvc mockMvc;

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.with(ADMIN).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    @Test
    void requestsWithoutCredentialsAreRejected() throws Exception {
        mockMvc.perform(get("/users")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/users").with(httpBasic("admin", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listsSeededUsersWithLinks() throws Exception {
        mockMvc.perform(get("/users").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.userList", hasSize(3)))
                .andExpect(jsonPath("$._embedded.userList[0].name").value("Maria"))
                .andExpect(jsonPath("$._links.self.href", endsWith("/users")));
    }

    @Test
    void returnsOneUserWithHateoasLinks() throws Exception {
        mockMvc.perform(get("/users/1001").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.birthDate").value("1995-04-12"))
                .andExpect(jsonPath("$._links.self.href", endsWith("/users/1001")))
                .andExpect(jsonPath("$._links.posts.href", endsWith("/users/1001/posts")))
                .andExpect(jsonPath("$._links.all-users.href", endsWith("/users")));
    }

    @Test
    void returnsXmlWhenAsked() throws Exception {
        mockMvc.perform(get("/users/1002/posts").with(ADMIN).accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(xpath("//description").string("Trying out HATEOAS links today"));
    }

    @Test
    void unknownUserIsAProblemDetail404() throws Exception {
        mockMvc.perform(get("/users/1").with(ADMIN))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("No user with id 1"));
    }

    @Test
    void createsAUserAndReturnsItsLocation() throws Exception {
        mockMvc.perform(json(post("/users"), "{\"name\":\"Petar\",\"birthDate\":\"1990-01-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                        "Location",
                                        org.hamcrest.Matchers.matchesPattern(".*/users/\\d+")))
                .andExpect(jsonPath("$.name").value("Petar"));
        mockMvc.perform(get("/users").with(ADMIN))
                .andExpect(jsonPath("$._embedded.userList", hasSize(4)));
    }

    @Test
    void rejectsAnInvalidUser() throws Exception {
        mockMvc.perform(json(post("/users"), "{\"name\":\"P\",\"birthDate\":\"2999-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void deletesAUserTogetherWithTheirPosts() throws Exception {
        mockMvc.perform(delete("/users/1001").with(ADMIN)).andExpect(status().isNoContent());
        mockMvc.perform(get("/users/1001").with(ADMIN)).andExpect(status().isNotFound());
    }

    @Test
    void addsAndListsPosts() throws Exception {
        mockMvc.perform(
                        json(
                                post("/users/1003/posts"),
                                "{\"description\":\"My very first post here\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("My very first post here"));
        mockMvc.perform(get("/users/1003/posts").with(ADMIN)).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(json(post("/users/1003/posts"), "{\"description\":\"short\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(
                        json(
                                post("/users/5/posts"),
                                "{\"description\":\"A post for nobody at all\"}"))
                .andExpect(status().isNotFound());
    }
}
