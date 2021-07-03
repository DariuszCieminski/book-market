package pl.bookmarket.validation.handlers;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pl.bookmarket.dto.ErrorDto;
import pl.bookmarket.validation.exceptions.CustomException;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;
import pl.bookmarket.validation.exceptions.EntityValidationException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(EntityValidationException.class)
    public ResponseEntity<Object> handleEntityValidationException(EntityValidationException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                             .body(Collections.singletonMap("errors", Collections.singletonList(e.getError())));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CustomException> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getHttpStatus()).body(e);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatus status, WebRequest request) {
        String message = "Invalid ID";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        List<ErrorDto> errorDtoList = ex.getFieldErrors().stream()
                                        .map(error -> new ErrorDto(error.getField(), error.getDefaultMessage()))
                                        .collect(Collectors.toList());
        Map<String, List<ErrorDto>> responseBody = Collections.singletonMap("errors", errorDtoList);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(responseBody);
    }
}