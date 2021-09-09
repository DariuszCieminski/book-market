package pl.bookmarket.model;

import com.fasterxml.jackson.annotation.JsonView;
import pl.bookmarket.util.Views;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offerGenerator")
    @SequenceGenerator(name = "offerGenerator", sequenceName = "offer_sequence", allocationSize = 1)
    @JsonView({Views.Market.class, Views.Offer.class})
    private Long id;

    @Transient
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "book_id", nullable = false)
    @JsonView({Views.Market.class, Views.Offer.class})
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    @JsonView({Views.Market.class, Views.Offer.class})
    private User buyer;

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

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }
}