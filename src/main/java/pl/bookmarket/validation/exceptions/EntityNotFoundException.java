package pl.bookmarket.validation.exceptions;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(Class<?> entityClass, Long id) {
        super(String.format("The %s with id \"%d\" does not exist.", entityClass.getSimpleName(), id));
    }
}