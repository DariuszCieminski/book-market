package pl.bookmarket.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.bookmarket.model.Message;
import pl.bookmarket.service.crud.MessageService;
import pl.bookmarket.util.Views;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    @JsonView(Views.Message.class)
    public List<Message> getReceivedMessages(Authentication authentication) {
        return messageService.getReceivedMessages(authentication.getName());
    }

    @GetMapping("/all")
    @JsonView(Views.Message.class)
    public List<Message> getAllMessages(Authentication authentication) {
        return messageService.getAllMessages(authentication.getName());
    }

    @GetMapping("/unread")
    @JsonView(Views.Message.class)
    public List<Message> getUnreadMessages(Authentication authentication) {
        return messageService.getUnreadMessages(authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @JsonView(Views.Message.class)
    public Message sendMessage(@Valid @RequestBody Message message) {
        return messageService.createMessage(message);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setMessagesReadForUser(@RequestParam("ids") List<Long> messageIdsList) {
        messageService.setMessagesRead(messageIdsList);
    }
}