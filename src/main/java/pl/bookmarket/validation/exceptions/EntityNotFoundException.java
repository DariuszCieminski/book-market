package pl.bookmarket.validation.exceptions;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(Class<?> entityClass) {
        super(String.format("The %s does not exist.", entityClass.getSimpleName()));
    }
}