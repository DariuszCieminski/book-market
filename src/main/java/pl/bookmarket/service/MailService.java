package pl.bookmarket.service;

import java.util.Map;
import pl.bookmarket.util.MailType;

public interface MailService {

    void sendMessage(String recipient, MailType mailType, Map<String, String> variables);
}