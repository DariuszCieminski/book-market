package pl.bookmarket.validation.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pl.bookmarket.validation.exceptions.CustomException;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;
import pl.bookmarket.validation.exceptions.ValidationException;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    @Autowired
    public ControllerExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        return ResponseEntity.status(status).body(new CustomException(ex.getLocalizedMessage(), status));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CustomException> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getHttpStatus()).body(e);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException e) {
        Map<String, ?> errorOutput;

        if (e.getErrorMessage() != null) {
            errorOutput = Collections.singletonMap("error", messageSource.getMessage(
                e.getErrorMessage(), null, e.getErrorMessage(), LocaleContextHolder.getLocale()));
        } else {
            List<Map<String, String>> list = new ArrayList<>();

            for (FieldError error : e.getErrors()) {
                Map<String, String> map = new HashMap<>(2, 1.0f);
                map.put("name", error.getField());
                map.put("status", error.getDefaultMessage());
                list.add(map);
            }

            errorOutput = Collections.singletonMap("fieldErrors", list);
        }

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorOutput);
    }
}