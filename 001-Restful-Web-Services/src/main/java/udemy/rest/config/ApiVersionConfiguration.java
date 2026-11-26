package udemy.rest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// clients pick a version with a header, a request parameter or a media type parameter
@Configuration
public class ApiVersionConfiguration implements WebMvcConfigurer {

    public static final MediaType PERSON =
            MediaType.parseMediaType("application/vnd.udemy.person+json");

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                .useRequestHeader("X-API-Version")
                .useQueryParam("version")
                .useMediaTypeParameter(PERSON, "v")
                .setVersionRequired(false)
                .setDefaultVersion("1");
    }
}
