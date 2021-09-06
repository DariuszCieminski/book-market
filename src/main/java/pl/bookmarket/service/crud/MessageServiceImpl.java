package pl.bookmarket.service.crud;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.bookmarket.dao.MessageDao;
import pl.bookmarket.model.Message;
import pl.bookmarket.model.User;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;
import pl.bookmarket.validation.exceptions.EntityValidationException;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageDao messageDao;
    private final UserService userService;

    public MessageServiceImpl(MessageDao messageDao, UserService userService) {
        this.messageDao = messageDao;
        this.userService = userService;
    }

    @Override
    public List<Message> getReceivedMessages(String login) {
        return messageDao.findMessagesByReceiverLogin(login);
    }

    @Override
    public List<Message> getUnreadMessages(String login) {
        return messageDao.findMessagesByReadFalseAndReceiverLogin(login);
    }

    @Override
    public List<Message> getAllMessages(String login) {
        return messageDao.findAllMessagesForUser(login);
    }

    @Override
    public Message createMessage(Message message) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User receiver = userService.getUserByLogin(message.getReceiver().getLogin());

        if (receiver == null || receiver.getLogin().equals(authentication.getName())) {
            throw new EntityValidationException("receiver", "receiver.invalid");
        }

        User sender = userService.getUserByLogin(authentication.getName());
        message.setReceiver(receiver);
        message.setSender(sender);
        message.setRead(false);

        return messageDao.save(message);
    }

    @Override
    public List<Message> createMultipleMessages(List<Message> messages) {
        return (List<Message>) messageDao.saveAll(messages);
    }

    @Override
    public Message updateMessage(Message message) {
        Message msg = messageDao.findById(message.getId())
                                .orElseThrow(() -> new EntityNotFoundException(Message.class));
        msg.setText(message.getText());
        msg.setRead(message.isRead());
        return messageDao.save(msg);
    }

    @Override
    public void setMessagesRead(List<Long> messageIds) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Iterable<Message> messages = messageDao.findAllById(messageIds);

        messages.forEach(message -> {
            if (message.getReceiver().getLogin().equals(authentication.getName())) {
                message.setRead(true);
            }
        });

        messageDao.saveAll(messages);
    }

    @Override
    public void deleteMessage(Long id) {
        messageDao.deleteById(id);
    }
}