package pl.bookmarket.controller;

import com.fasterxml.jackson.annotation.JsonView;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.bookmarket.dao.MessageDao;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.model.Message;
import pl.bookmarket.model.User;
import pl.bookmarket.util.Views;
import pl.bookmarket.validation.ValidationGroups;
import pl.bookmarket.validation.exceptions.ValidationException;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageDao messageDao;
    private final UserDao userDao;

    @Autowired
    public MessageController(MessageDao messageDao, UserDao userDao) {
        this.messageDao = messageDao;
        this.userDao = userDao;
    }

    @GetMapping
    @JsonView(Views.Message.class)
    public List<Message> getReceivedMessages(Authentication authentication) {
        return messageDao.findMessagesByReceiver_Login(authentication.getName());
    }

    @GetMapping("/all")
    @JsonView(Views.Message.class)
    public List<Message> getAllMessages(Authentication authentication) {
        return messageDao.findAllMessages(authentication.getName());
    }

    @GetMapping("/unread")
    @JsonView(Views.Message.class)
    public List<Message> getUnreadMessages(Authentication authentication) {
        return messageDao.findMessagesByReadFalseAndReceiver_Login(authentication.getName());
    }

    @GetMapping("/users")
    public List<String> getUserLogins(Authentication authentication) {
        return userDao.getUserLogins(authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @JsonView(Views.Message.class)
    public Message sendMessage(@Validated(ValidationGroups.SendMessage.class) @RequestBody Message message,
                               BindingResult result, Authentication authentication) {
        if (result.hasErrors()) {
            throw new ValidationException(result.getFieldErrors());
        }

        User receiver = userDao.findUserByLogin(message.getReceiver().getLogin());

        if (receiver == null || receiver.getLogin().equals(authentication.getName())) {
            throw new ValidationException("user.invalid");
        }

        message.setReceiver(receiver);
        message.setSender(userDao.findUserByLogin(authentication.getName()));
        message.setRead(false);

        return messageDao.save(message);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setMessagesReadForUser(@RequestParam("ids") List<Long> messageIdsList, Authentication authentication) {
        Iterable<Message> messages = messageDao.findAllById(messageIdsList);

        messages.forEach(message -> {
            if (message.getReceiver().getLogin().equals(authentication.getName())) {
                message.setRead(true);
            }
        });

        messageDao.saveAll(messages);
    }
}