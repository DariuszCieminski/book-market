package pl.bookmarket.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class Error implements ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        Integer errorStatus = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorMessage = null;

        if (errorStatus == HttpServletResponse.SC_UNAUTHORIZED) {
            response.sendRedirect("/login");
        } else if (errorStatus == HttpServletResponse.SC_FORBIDDEN) {
            errorMessage = "error.forbidden";
        } else if (errorStatus == HttpServletResponse.SC_NOT_FOUND) {
            errorMessage = "error.notfound";
        } else {
            errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        }

        model.addAttribute("errorStatus", errorStatus);
        model.addAttribute("errorMessage", errorMessage);

        return "error";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}