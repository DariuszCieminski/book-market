package pl.bookmarket.validation.exceptions;

import org.springframework.validation.FieldError;

import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<FieldError> errors;
    private final String errorMessage;

    public ValidationException(List<FieldError> validationErrors) {
        this.errors = validationErrors;
        errorMessage = null;
    }

    public ValidationException(String errorMessage) {
        this.errorMessage = errorMessage;
        errors = null;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}