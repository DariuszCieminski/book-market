package pl.bookmarket.service.email;

import java.util.Map;
import javax.mail.internet.InternetAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import pl.bookmarket.util.MailType;

@Service
@Profile("!mailDisabled")
public class MailServiceImpl implements MailService {

    private final JavaMailSenderImpl sender;
    private final MailTemplateResolver templateResolver;
    private final MessageSource messageSource;

    @Autowired
    public MailServiceImpl(JavaMailSenderImpl sender, MailTemplateResolver templateResolver, MessageSource messageSource) {
        this.sender = sender;
        this.templateResolver = templateResolver;
        this.messageSource = messageSource;
    }

    @Override
    public void sendMessage(String recipient, MailType mailType, Map<String, String> variables) {
        MimeMessagePreparator preparator = message -> {
            MimeMessageHelper messageHelper =
                    new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            messageHelper.setFrom(new InternetAddress("noreply@book-market.pl",
                                                      messageSource.getMessage("app", null,
                                                                               LocaleContextHolder.getLocale())));
            messageHelper.setTo(recipient);
            messageHelper.setSubject(messageSource.getMessage(mailType.name().toLowerCase(), null,
                                                              LocaleContextHolder.getLocale()));
            messageHelper.setText(templateResolver.resolveTemplate(mailType, variables), true);
        };

        sender.send(preparator);
    }
}