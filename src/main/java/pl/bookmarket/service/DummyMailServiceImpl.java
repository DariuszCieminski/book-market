package pl.bookmarket.service;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import pl.bookmarket.util.MailType;

@Service
@Profile("mailDisabled")
public class DummyMailServiceImpl implements MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    @Override
    public void sendMessage(String recipient, MailType mailType, Map<String, String> variables) {
        logger.info("The {} mail was sent to recipient ({}) with variables: {}",
                    mailType.name(),
                    recipient,
                    variableMapToString(variables));
    }

    private String variableMapToString(Map<String, String> variables) {
        if (variables == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder("{ ");
        Iterator<Entry<String, String>> iterator = variables.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            builder.append(entry.getKey()).append("=").append(entry.getValue());
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        builder.append(" }");
        return builder.toString();
    }
}