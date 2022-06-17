package pl.bookmarket.validation.exception;

import pl.bookmarket.dto.ErrorDto;

public class EntityNotFoundException extends RuntimeException {

    private final ErrorDto error;

    public EntityNotFoundException(Class<?> entityClass) {
        super(String.format("The %s does not exist.", entityClass.getSimpleName()));
        this.error = new ErrorDto(String.format("%s.id", entityClass.getSimpleName().toLowerCase()), "not.found");
    }

    public ErrorDto getError() {
        return error;
    }
}