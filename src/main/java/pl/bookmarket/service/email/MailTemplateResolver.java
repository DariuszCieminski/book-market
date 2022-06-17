package pl.bookmarket.service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailTemplateResolver {

    private static final String NULL_VALUE = "<NULL>";
    private final TemplateEngine templateEngine;

    @Autowired
    public MailTemplateResolver(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String resolveTemplate(Mailable mailable) {
        Context context = new Context();
        context.setLocale(LocaleContextHolder.getLocale());
        mailable.getTemplateVariables().forEach((k, v) -> context.setVariable(k, v == null ? NULL_VALUE : v));
        return templateEngine.process(mailable.getTemplateName(), context);
    }
}