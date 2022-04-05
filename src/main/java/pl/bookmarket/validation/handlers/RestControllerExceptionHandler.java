package pl.bookmarket.validation.handlers;

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
import pl.bookmarket.validation.exceptions.EntityNotFoundException;
import pl.bookmarket.validation.exceptions.EntityValidationException;

import java.util.List;
import java.util.Map;
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

    @ExceptionHandler(BearerTokenException.class)
    public ResponseEntity<String> handleBearerTokenException(BearerTokenException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        List<ErrorDto> errorDtoList = ex.getFieldErrors().stream()
                                        .map(error -> new ErrorDto(error.getField(), error.getDefaultMessage()))
                                        .collect(Collectors.toList());
        Map<String, List<ErrorDto>> responseBody = singletonMap("errors", errorDtoList);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(responseBody);
    }
}