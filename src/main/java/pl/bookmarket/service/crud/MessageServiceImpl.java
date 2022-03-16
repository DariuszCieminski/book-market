package pl.bookmarket.service.crud;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bookmarket.dao.MessageDao;
import pl.bookmarket.model.Message;
import pl.bookmarket.model.User;
import pl.bookmarket.security.authentication.AuthenticatedUser;
import pl.bookmarket.util.AuthUtils;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;
import pl.bookmarket.validation.exceptions.EntityValidationException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

@Service
@Transactional(readOnly = true)
public class MessageServiceImpl implements MessageService {
    private final MessageDao messageDao;
    private final UserService userService;

    public MessageServiceImpl(MessageDao messageDao, UserService userService) {
        this.messageDao = messageDao;
        this.userService = userService;
    }

    @Override
    @PreAuthorize("authentication.principal.id == #userId")
    public List<Message> getReceivedMessages(Long userId) {
        return messageDao.findMessagesByReceiverId(userId);
    }

    @Override
    @PreAuthorize("authentication.principal.id == #userId")
    public List<Message> getUnreadMessages(Long userId) {
        return messageDao.findMessagesByReadFalseAndReceiverId(userId);
    }

    @Override
    @PreAuthorize("authentication.principal.id == #userId")
    public List<Message> getAllMessages(Long userId) {
        return messageDao.findAllMessagesForUser(userId);
    }

    @Override
    @Transactional
    public Message createMessage(Message message) {
        validateAndUpdateMessage(message);
        return messageDao.save(message);
    }

    @Override
    @Transactional
    public List<Message> createMultipleMessages(List<Message> messages) {
        messages.forEach(this::validateAndUpdateMessage);
        return (List<Message>) messageDao.saveAll(messages);
    }

    @Override
    @Transactional
    public Message updateMessage(Message message) {
        Message msg = messageDao.findById(message.getId())
                                .orElseThrow(() -> new EntityNotFoundException(Message.class));
        verifyUserPermissions(message);
        msg.setText(message.getText());
        msg.setRead(message.isRead());
        return messageDao.save(msg);
    }

    @Override
    @Transactional
    public void setMessagesRead(List<Long> messageIds) {
        Iterable<Message> messages = messageDao.findAllById(messageIds);
        AuthenticatedUser currentUser = AuthUtils.getCurrentUser(AuthenticatedUser.class);

        messages.forEach(message -> {
            if (currentUser.getId().equals(message.getReceiver().getId())) {
                message.setRead(true);
            }
        });

        messageDao.saveAll(messages);
    }

    @Override
    @Transactional
    public void deleteMessage(Long id) {
        Message message = messageDao.findById(id).orElseThrow(() -> new EntityNotFoundException(Message.class));
        verifyUserPermissions(message);
        messageDao.delete(message);
    }

    private void verifyUserPermissions(Message message) {
        Predicate<AuthenticatedUser> predicate = user -> message.getReceiver().getId().equals(user.getId());

        if (!AuthUtils.hasAccess(AuthenticatedUser.class, predicate)) {
            throw new AccessDeniedException("The current user cannot perform this action.");
        }
    }

    private void validateAndUpdateMessage(Message message) {
        AuthenticatedUser currentUser = AuthUtils.getCurrentUser(AuthenticatedUser.class);
        Optional<User> receiver = userService.getUserById(message.getReceiver().getId());

        if (!receiver.isPresent() || receiver.get().getId().equals(currentUser.getId())) {
            throw new EntityValidationException("receiver", "receiver.invalid");
        }

        boolean senderInvalid = message.getSender() == null || message.getSender().getId() == null;

        // set sender to current user if it's invalid or set by ineligible user
        if (senderInvalid || !AuthUtils.isAdmin(currentUser)) {
            User sender = userService.getUserById(currentUser.getId()).orElseThrow(NoSuchElementException::new);
            message.setSender(sender);
        }
        message.setReceiver(receiver.get());
        message.setRead(false);
    }
}