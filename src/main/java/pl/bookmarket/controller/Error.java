package pl.bookmarket.controller;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import pl.bookmarket.validation.exceptions.CustomException;

@Controller
public class Error implements ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        switch (status) {
            default:
                throw new CustomException((String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE),
                                          Objects.requireNonNull(HttpStatus.resolve(status)));
            case HttpServletResponse.SC_UNAUTHORIZED:
                response.sendRedirect("/login");
            case HttpServletResponse.SC_FORBIDDEN:
                return "denied";
            case HttpServletResponse.SC_NOT_FOUND:
                return "notfound";
        }
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}