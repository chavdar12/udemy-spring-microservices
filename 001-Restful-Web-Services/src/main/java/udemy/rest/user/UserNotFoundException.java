package udemy.rest.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

// rendered as an RFC 9457 problem detail, the same shape Spring uses for validation errors
public class UserNotFoundException extends ErrorResponseException {

    public UserNotFoundException(long id) {
        super(
                HttpStatus.NOT_FOUND,
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "No user with id " + id),
                null);
    }
}
