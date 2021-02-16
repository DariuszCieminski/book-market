package pl.bookmarket.service;

import java.util.Map;
import org.springframework.scheduling.annotation.Async;
import pl.bookmarket.util.MailType;

public interface MailService {

    @Async
    void sendMessage(String recipient, MailType mailType, Map<String, String> variables);
}