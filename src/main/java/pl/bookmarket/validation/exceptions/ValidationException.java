package pl.bookmarket.validation.exceptions;

import java.util.List;
import org.springframework.validation.FieldError;

public class ValidationException extends RuntimeException {

    private List<FieldError> errors;
    private String errorMessage;

    public ValidationException(List<FieldError> validationErrors) {
        this.errors = validationErrors;
    }

    public ValidationException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}