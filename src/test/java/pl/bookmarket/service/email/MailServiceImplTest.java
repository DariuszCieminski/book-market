package pl.bookmarket.service.email;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.util.io.IOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import pl.bookmarket.model.User;
import pl.bookmarket.service.email.template.AccountCreatedMail;
import pl.bookmarket.testhelpers.datafactory.UserBuilder;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.InputStream;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.AFTER_METHOD;

@SpringBootTest(properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=25"
})
class MailServiceImplTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetup.SMTP);

    @Autowired
    private MailService mailService;

    @Autowired
    private MessageSource messageSource;

    @ParameterizedTest
    @MethodSource("getEmailTestData")
    @DirtiesContext(methodMode = AFTER_METHOD)
    void shouldSuccessfullySendEMail(Mailable mailable, Locale locale, String[] localeArgs) throws Exception {
        LocaleContextHolder.setLocale(locale, true);
        String email = GreenMailUtil.random();

        mailService.sendMail(mailable, email);
        greenMail.waitForIncomingEmail(1);

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        MimeMessage message = messages[0];
        InputStream decodedMessage = MimeUtility.decode(message.getInputStream(), "quoted-printable");
        String messageBody = String.join("", IOUtil.readLines(decodedMessage));
        assertEquals("Book Market <noreply@book-market.pl>", message.getFrom()[0].toString());
        assertEquals(email, message.getAllRecipients()[0].toString());
        assertEquals(mailable.getMailTitle(), message.getSubject());
        for (String value : mailable.getTemplateVariables().values()) {
            assertTrue(messageBody.contains(value));
        }
        for (String localeArg : localeArgs) {
            String localeMessage = messageSource.getMessage(localeArg, null, locale);
            for (String part : localeMessage.split("\\{\\d+}")) {
                if (!part.isEmpty()) {
                    assertTrue(messageBody.contains(part));
                }
            }
        }
    }

    @Test
    void shouldUsePlaceholderInTemplateInsteadOfNullValue() throws Exception {
        Mailable accountCreatedMail = new AccountCreatedMail(null, "P@s$w0rd");
        Locale locale = new Locale("pl");
        LocaleContextHolder.setLocale(locale, true);

        mailService.sendMail(accountCreatedMail, GreenMailUtil.random());
        greenMail.waitForIncomingEmail(1);

        assertEquals(1, greenMail.getReceivedMessages().length);
        MimeMessage message = greenMail.getReceivedMessages()[0];
        InputStream decodedMessage = MimeUtility.decode(message.getInputStream(), "quoted-printable");
        String messageBody = String.join("", IOUtil.readLines(decodedMessage));
        assertTrue(messageBody.contains(messageSource.getMessage("greeting.message", new String[] {"&lt;NULL&gt;"}, locale)));
        assertTrue(messageBody.contains(messageSource.getMessage("user.password", new String[] {"P@s$w0rd"}, locale)));
    }

    private static Stream<Arguments> getEmailTestData() {
        User user = UserBuilder.getDefaultUser().build();
        Mailable accountCreatedMail = new AccountCreatedMail(user.getLogin(), user.getPassword());
        String[] accountCreatedVars = {"greeting.message", "account.created", "user.password", "pwd.change.reminder", "app.enjoyment.wish"};

        return Stream.of(
                Arguments.of(accountCreatedMail, new Locale("pl"), accountCreatedVars),
                Arguments.of(accountCreatedMail, new Locale("en"), accountCreatedVars)
        );
    }
}