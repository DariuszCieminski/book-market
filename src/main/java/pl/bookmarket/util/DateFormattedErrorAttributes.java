package pl.bookmarket.util;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.text.SimpleDateFormat;
import java.util.Map;

@Component
public class DateFormattedErrorAttributes extends DefaultErrorAttributes {

    private final SimpleDateFormat dateFormat;

    public DateFormattedErrorAttributes() {
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    }

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        errorAttributes.computeIfPresent("timestamp", (str, obj) -> obj = dateFormat.format(obj));
        return errorAttributes;
    }
}