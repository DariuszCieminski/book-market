package pl.bookmarket.service.email;

import org.springframework.scheduling.annotation.Async;

public interface MailService {
    @Async
    void sendMail(Mailable mailable, String recipient);
}