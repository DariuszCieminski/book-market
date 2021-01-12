package pl.bookmarket.validation.handlers;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

public class AccessDenied implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(AccessDenied.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
        throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            logger.warn("User {} was trying to open {}", authentication.getName(), request.getRequestURI());
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
}