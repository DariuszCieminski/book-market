package pl.bookmarket.dto;

import pl.bookmarket.validation.ValidationGroups;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class MessageCreateDto {

    @NotBlank(message = "message.empty")
    @Size(max = 300, message = "message.too.long")
    private String text;

    @NotNull(message = "receiver.invalid", groups = ValidationGroups.OnCreate.class)
    private UserSimpleDto receiver;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public UserSimpleDto getReceiver() {
        return receiver;
    }

    public void setReceiver(UserSimpleDto receiver) {
        this.receiver = receiver;
    }
}