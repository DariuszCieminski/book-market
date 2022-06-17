package pl.bookmarket.service.crud;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.bookmarket.dao.MessageDao;
import pl.bookmarket.model.Message;
import pl.bookmarket.model.User;
import pl.bookmarket.testhelpers.datafactory.AuthenticationFactory;
import pl.bookmarket.testhelpers.datafactory.MessageBuilder;
import pl.bookmarket.testhelpers.datafactory.UserBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(SpringExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageDao messageDao;

    @Mock
    private UserService userService;

    @InjectMocks
    private MessageServiceImpl messageService;

    @Test
    void shouldNotModifySenderWhenCreatingMessageAsAdmin() {
        UserBuilder adminUserBuilder = UserBuilder.getAdminUser().withId(3L);
        Authentication adminUserAuthentication = AuthenticationFactory.getAuthenticationFromUser(adminUserBuilder);
        SecurityContextHolder.getContext().setAuthentication(adminUserAuthentication);
        Message message = MessageBuilder.getDefaultMessage().withId(null).build();
        Mockito.when(userService.getUserById(message.getReceiver().getId()))
               .thenReturn(Optional.of(message.getReceiver()));
        Mockito.when(messageDao.save(ArgumentMatchers.any(Message.class)))
               .thenAnswer(invocation -> invocation.getArgument(0));

        Message created = messageService.createMessage(message);

        assertEquals(message.getSender().getId(), created.getSender().getId());
        assertEquals(message.getReceiver().getId(), created.getReceiver().getId());
        assertEquals(message.getText(), created.getText());
        assertFalse(message.isRead());
    }

    @Test
    void shouldSetCurrentUserAsMessageSenderWhenCreatingMessageAsNonAdmin() {
        Long currentUserId = 3L;
        UserBuilder regularUserBuilder = UserBuilder.getDefaultUser().withId(currentUserId);
        Authentication regularUserAuthentication = AuthenticationFactory.getAuthenticationFromUser(regularUserBuilder);
        SecurityContextHolder.getContext().setAuthentication(regularUserAuthentication);
        Message message = MessageBuilder.getDefaultMessage().withId(null).build();
        Mockito.when(userService.getUserById(message.getReceiver().getId()))
               .thenReturn(Optional.of(message.getReceiver()));
        Mockito.when(userService.getUserById(currentUserId))
               .thenReturn(Optional.of(regularUserBuilder.build()));
        Mockito.when(messageDao.save(ArgumentMatchers.any(Message.class)))
               .thenAnswer(invocation -> invocation.getArgument(0));

        Message created = messageService.createMessage(message);

        assertEquals(currentUserId, created.getSender().getId());
        assertEquals(message.getReceiver().getId(), created.getReceiver().getId());
        assertEquals(message.getText(), created.getText());
        assertFalse(message.isRead());
    }

    @ParameterizedTest
    @MethodSource("getInvalidSenders")
    void shouldSetCurrentUserAsMessageSenderWhenCreatingMessageWithInvalidSender(User sender) {
        Long currentUserId = 3L;
        UserBuilder adminUserBuilder = UserBuilder.getAdminUser().withId(currentUserId);
        Authentication adminUserAuthentication = AuthenticationFactory.getAuthenticationFromUser(adminUserBuilder);
        SecurityContextHolder.getContext().setAuthentication(adminUserAuthentication);
        Message message = MessageBuilder.getDefaultMessage().withId(null).withSender(sender).build();
        Mockito.when(userService.getUserById(message.getReceiver().getId()))
               .thenReturn(Optional.of(message.getReceiver()));
        Mockito.when(userService.getUserById(currentUserId))
               .thenReturn(Optional.of(adminUserBuilder.build()));
        Mockito.when(messageDao.save(ArgumentMatchers.any(Message.class)))
               .thenAnswer(invocation -> invocation.getArgument(0));

        Message created = messageService.createMessage(message);

        assertEquals(currentUserId, created.getSender().getId());
        assertEquals(message.getReceiver().getId(), created.getReceiver().getId());
        assertEquals(message.getText(), created.getText());
        assertFalse(message.isRead());
    }

    private static List<User> getInvalidSenders() {
        return Arrays.asList(null, UserBuilder.getDefaultUser().withId(null).build());
    }
}