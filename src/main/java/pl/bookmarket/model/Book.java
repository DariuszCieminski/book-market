package pl.bookmarket.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import java.math.BigDecimal;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import pl.bookmarket.util.Views;

@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bookGenerator")
    @SequenceGenerator(name = "bookGenerator", sequenceName = "book_sequence", initialValue = 1000, allocationSize = 1)
    @JsonView({Views.Books.class, Views.Market.class, Views.Offer.class})
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "{field.not.blank}")
    @JsonView({Views.Books.class, Views.Market.class, Views.Offer.class})
    private String title;

    @Column(nullable = false)
    @NotBlank(message = "{field.not.blank}")
    @JsonView({Views.Books.class, Views.Market.class})
    private String author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id")
    @NotNull(message = "{field.not.blank}")
    @JsonIgnoreProperties("books")
    @JsonView({Views.Books.class, Views.Market.class})
    private Genre genre;

    @NotNull(message = "{field.not.blank}")
    @Positive(message = "{field.positive.value}")
    @JsonView({Views.Books.class, Views.Market.class})
    private Integer pages;

    @NotBlank(message = "{field.not.blank}")
    @JsonView({Views.Books.class, Views.Market.class})
    private String publisher;

    @Column(name = "release_year")
    @NotNull(message = "{field.not.blank}")
    @Positive(message = "{field.positive.value}")
    @JsonView({Views.Books.class, Views.Market.class})
    private Integer releaseYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonView(Views.Market.class)
    private User owner;

    @Column(name = "for_sale")
    @JsonView(Views.Books.class)
    private boolean forSale;

    @Column(precision = 10, scale = 2)
    @Digits(integer = 10, fraction = 2, message = "{price.invalid.precision}")
    @PositiveOrZero(message = "{price.less.than.zero}")
    @JsonView({Views.Books.class, Views.Market.class})
    private BigDecimal price;

    @OneToMany(mappedBy = "book", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private Set<Offer> offers;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public boolean isForSale() {
        return forSale;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Set<Offer> getOffers() {
        return offers;
    }

    public void setOffers(Set<Offer> offers) {
        this.offers = offers;
    }
}