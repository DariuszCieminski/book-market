package pl.bookmarket.dto;

import pl.bookmarket.validation.constraint.YearMaxCurrent;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class BookCreateDto {

    @NotBlank(message = "field.blank")
    private String title;

    @NotBlank(message = "field.blank")
    private String author;

    @NotNull(message = "id.invalid")
    private Long genreId;

    @NotNull(message = "field.blank")
    @Positive(message = "field.not.positive.value")
    private Integer pages;

    @NotBlank(message = "field.blank")
    private String publisher;

    @NotNull(message = "field.blank")
    @Min(value = 1500, message = "year.too.low")
    @YearMaxCurrent(message = "year.too.high")
    private Integer releaseYear;

    private boolean forSale;

    @Digits(integer = 5, fraction = 2, message = "price.invalid.precision")
    @PositiveOrZero(message = "price.negative")
    private BigDecimal price;

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

    public Long getGenreId() {
        return genreId;
    }

    public void setGenreId(Long genreId) {
        this.genreId = genreId;
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
}