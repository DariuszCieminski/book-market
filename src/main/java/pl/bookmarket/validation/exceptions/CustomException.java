package pl.bookmarket.validation.exceptions;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;

@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.NONE,
                getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE,
                setterVisibility = JsonAutoDetect.Visibility.NONE)
public class CustomException extends RuntimeException {

    @JsonProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm:ss")
    private final OffsetDateTime timestamp;

    @JsonProperty
    private final String status;

    @JsonProperty
    private final String message;

    @JsonIgnore
    private final HttpStatus httpStatus;

    public CustomException(String message, HttpStatus httpStatus) {
        this.timestamp = OffsetDateTime.now();
        this.status = String.format("%d (%s)", httpStatus.value(), httpStatus.getReasonPhrase());
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}