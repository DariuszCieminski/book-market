package pl.bookmarket.service.email;

import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import javax.mail.internet.InternetAddress;

@Service
@Profile("!mailDisabled")
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final MailTemplateResolver templateResolver;

    public MailServiceImpl(JavaMailSender mailSender, MailTemplateResolver templateResolver) {
        this.mailSender = mailSender;
        this.templateResolver = templateResolver;
    }

    @Override
    public void sendMail(Mailable mailable, String recipient) {
        MimeMessagePreparator messagePreparator = message -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
            messageHelper.setFrom(new InternetAddress("noreply@book-market.pl", "Book Market"));
            messageHelper.setTo(recipient);
            messageHelper.setSubject(mailable.getMailTitle());
            messageHelper.setText(templateResolver.resolveTemplate(mailable), true);
        };

        mailSender.send(messagePreparator);
    }
}