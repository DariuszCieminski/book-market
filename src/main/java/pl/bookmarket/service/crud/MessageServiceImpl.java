package pl.bookmarket.service.crud;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import pl.bookmarket.dao.MessageDao;
import pl.bookmarket.model.Message;
import pl.bookmarket.model.User;
import pl.bookmarket.service.authentication.AuthenticatedUser;
import pl.bookmarket.util.AuthUtils;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;
import pl.bookmarket.validation.exceptions.EntityValidationException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageDao messageDao;
    private final UserService userService;

    public MessageServiceImpl(MessageDao messageDao, UserService userService) {
        this.messageDao = messageDao;
        this.userService = userService;
    }

    @Override
    public List<Message> getReceivedMessages(Long userId) {
        verifyUserPermissions(userId);
        return messageDao.findMessagesByReceiverId(userId);
    }

    @Override
    public List<Message> getUnreadMessages(Long userId) {
        verifyUserPermissions(userId);
        return messageDao.findMessagesByReadFalseAndReceiverId(userId);
    }

    @Override
    public List<Message> getAllMessages(Long userId) {
        verifyUserPermissions(userId);
        return messageDao.findAllMessagesForUser(userId);
    }

    @Override
    public Message createMessage(Message message) {
        validateAndUpdateMessage(message);
        return messageDao.save(message);
    }

    @Override
    public List<Message> createMultipleMessages(List<Message> messages) {
        messages.forEach(this::validateAndUpdateMessage);
        return (List<Message>) messageDao.saveAll(messages);
    }

    @Override
    public Message updateMessage(Message message) {
        Message msg = messageDao.findById(message.getId())
                                .orElseThrow(() -> new EntityNotFoundException(Message.class));
        verifyUserPermissions(message.getReceiver().getId());
        msg.setText(message.getText());
        msg.setRead(message.isRead());
        return messageDao.save(msg);
    }

    @Override
    public void setMessagesRead(List<Long> messageIds) {
        Iterable<Message> messages = messageDao.findAllById(messageIds);

        messages.forEach(message -> {
            if (AuthUtils.isAuthenticatedUserId(message.getReceiver().getId())) {
                message.setRead(true);
            }
        });

        messageDao.saveAll(messages);
    }

    @Override
    public void deleteMessage(Long id) {
        Message message = messageDao.findById(id).orElseThrow(() -> new EntityNotFoundException(Message.class));
        verifyUserPermissions(message.getReceiver().getId());
        messageDao.delete(message);
    }

    private void verifyUserPermissions(Long userId) {
        AuthenticatedUser authenticatedUser = AuthUtils.getAuthenticatedUser();
        boolean validUser = userId.equals(authenticatedUser.getId());

        if (!validUser && authenticatedUser.getAuthorities().size() <= 1) {
            throw new AccessDeniedException("The current user cannot perform this action.");
        }
    }

    private void validateAndUpdateMessage(Message message) {
        AuthenticatedUser currentUser = AuthUtils.getAuthenticatedUser();
        Optional<User> receiver = userService.getUserById(message.getReceiver().getId());

        if (!receiver.isPresent() || receiver.get().getId().equals(currentUser.getId())) {
            throw new EntityValidationException("receiver", "receiver.invalid");
        }

        boolean senderInvalid = message.getSender() == null || message.getSender().getId() == null;

        // set sender to current user if it's invalid or set by ineligible user
        if (senderInvalid || currentUser.getAuthorities().size() <= 1) {
            User sender = userService.getUserById(currentUser.getId()).orElseThrow(NoSuchElementException::new);
            message.setSender(sender);
        }
        message.setReceiver(receiver.get());
        message.setRead(false);
    }
}