package pl.bookmarket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.bookmarket.util.MailType;

import java.util.Map;

@Service
public class MailTemplateResolver {

    private final TemplateEngine templateEngine;

    @Autowired
    public MailTemplateResolver(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String resolveTemplate(MailType mailType, Map<String, String> variables) {
        Context context = new Context();
        context.setLocale(LocaleContextHolder.getLocale());

        switch (mailType) {
            case ACCOUNT_CREATED:
                context.setVariable("password", variables.getOrDefault("userPassword", "NULL"));
                return templateEngine.process("email-accountcreated", context);
            case PASSWORD_RESET:
                context.setVariable("password", variables.getOrDefault("userPassword", "NULL"));
                return templateEngine.process("email-passwordreset", context);
            default:
                return "";
        }
    }
}