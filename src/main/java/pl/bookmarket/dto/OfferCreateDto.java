package pl.bookmarket.dto;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

public class OfferCreateDto {

    @NotNull(message = "id.invalid")
    private Long bookId;

    @Length(min = 5, max = 300, message = "invalid.comment.length")
    private String comment;

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}