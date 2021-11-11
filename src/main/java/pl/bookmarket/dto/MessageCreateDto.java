package pl.bookmarket.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class MessageCreateDto {

    @NotBlank(message = "message.empty")
    @Size(max = 300, message = "message.too.long")
    private String text;

    @NotBlank(message = "login.empty")
    private String receiverLogin;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getReceiverLogin() {
        return receiverLogin;
    }

    public void setReceiverLogin(String receiverLogin) {
        this.receiverLogin = receiverLogin;
    }
}