package pl.bookmarket.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
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
    public List<Message> getMessages(@RequestParam(defaultValue = "all") String type, Authentication authentication) {
        if (type.equals("unread")) {
            return messageService.getUnreadMessages(authentication.getName());
        } else if (type.equals("received")) {
            return messageService.getReceivedMessages(authentication.getName());
        } else {
            return messageService.getAllMessages(authentication.getName());
        }
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