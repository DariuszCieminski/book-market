package pl.bookmarket.validation.exceptions;

import pl.bookmarket.dto.ErrorDto;

public class EntityValidationException extends RuntimeException {

    private final ErrorDto error;

    public EntityValidationException(String field, String errorCode) {
        this.error = new ErrorDto(field, errorCode);
    }

    public ErrorDto getError() {
        return error;
    }
}