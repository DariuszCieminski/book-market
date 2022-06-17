package pl.bookmarket.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

@Service
@Profile("mailDisabled")
public class DummyMailServiceImpl implements MailService {

    private static final Logger LOG = LoggerFactory.getLogger(DummyMailServiceImpl.class);

    @Override
    public void sendMail(Mailable mailable, String recipient) {
        String variablesString = mailVariablesToString(mailable.getTemplateVariables());
        LOG.info("The '{}' mail was sent to recipient ({}) with variables: {}", mailable.getTemplateName(), recipient, variablesString);
    }

    private String mailVariablesToString(Map<String, String> variablesMap) {
        if (variablesMap == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder("{ ");
        Iterator<Entry<String, String>> iterator = variablesMap.entrySet().iterator();

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