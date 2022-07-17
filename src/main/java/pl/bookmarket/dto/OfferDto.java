package pl.bookmarket.dto;

public class OfferDto {
    private Long id;
    private String comment;
    private BookDto book;
    private UserSimpleDto buyer;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public BookDto getBook() {
        return book;
    }

    public void setBook(BookDto book) {
        this.book = book;
    }

    public UserSimpleDto getBuyer() {
        return buyer;
    }

    public void setBuyer(UserSimpleDto buyer) {
        this.buyer = buyer;
    }
}