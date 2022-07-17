package pl.bookmarket.service.email.template;

import pl.bookmarket.service.email.Mailable;

import java.util.HashMap;
import java.util.Map;

public class AccountCreatedMail implements Mailable {

    private final Map<String, String> variables;

    public AccountCreatedMail(String login, String password) {
        this.variables = new HashMap<>();
        this.variables.put("login", login);
        this.variables.put("password", password);
    }

    @Override
    public String getMailTitle() {
        return "BOOK MARKET - Your account has been created";
    }

    @Override
    public String getTemplateName() {
        return "account_created";
    }

    @Override
    public Map<String, String> getTemplateVariables() {
        return variables;
    }
}