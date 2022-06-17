package pl.bookmarket.validation;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pl.bookmarket.dto.ErrorDto;
import pl.bookmarket.security.authentication.BearerTokenException;
import pl.bookmarket.validation.exception.EntityNotFoundException;
import pl.bookmarket.validation.exception.EntityValidationException;

import javax.validation.ConstraintViolationException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

@RestControllerAdvice
public class RestControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(singletonMap("errors", singletonList(e.getError())));
    }

    @ExceptionHandler(EntityValidationException.class)
    public ResponseEntity<Object> handleEntityValidationException(EntityValidationException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                             .body(singletonMap("errors", singletonList(e.getError())));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException e) {
        List<ErrorDto> errorDtoList = mapErrorCollectionToDto(e.getConstraintViolations(),
                violation -> new ErrorDto(violation.getPropertyPath().toString(), violation.getMessage()));
        Map<String, List<ErrorDto>> responseBody = singletonMap("errors", errorDtoList);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(responseBody);
    }

    @ExceptionHandler(BearerTokenException.class)
    public ResponseEntity<String> handleBearerTokenException(BearerTokenException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        List<ErrorDto> errorDtoList = mapErrorCollectionToDto(ex.getFieldErrors(),
                error -> new ErrorDto(error.getField(), error.getDefaultMessage()));
        Map<String, List<ErrorDto>> responseBody = singletonMap("errors", errorDtoList);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(responseBody);
    }

    private <E> List<ErrorDto> mapErrorCollectionToDto(Collection<E> errors, Function<E, ErrorDto> mapper) {
        return errors.stream().map(mapper).collect(Collectors.toList());
    }
}