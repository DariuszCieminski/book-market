package pl.bookmarket.service.email;

import java.util.Map;

public interface Mailable {
    String getMailTitle();

    String getTemplateName();

    Map<String, String> getTemplateVariables();
}