package pl.bookmarket.testhelpers.datafactory;

import pl.bookmarket.dto.BookCreateDto;
import pl.bookmarket.model.Book;
import pl.bookmarket.model.Genre;
import pl.bookmarket.model.Offer;
import pl.bookmarket.model.User;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

public class BookBuilder {

    private static long id = 100L;
    private final Book book = new Book();

    public Book build() {
        return book;
    }

    public BookCreateDto buildBookCreateDto() {
        BookCreateDto dto = new BookCreateDto();
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setPublisher(book.getPublisher());
        dto.setPages(book.getPages());
        dto.setReleaseYear(book.getReleaseYear());
        dto.setGenreId(book.getGenre() == null ? null : book.getGenre().getId());
        dto.setForSale(book.isForSale());
        dto.setPrice(book.getPrice());
        return dto;
    }

    public BookBuilder withId(Long id) {
        book.setId(id);
        return this;
    }

    public BookBuilder withTitle(String title) {
        book.setTitle(title);
        return this;
    }

    public BookBuilder withAuthor(String author) {
        book.setAuthor(author);
        return this;
    }

    public BookBuilder withGenre(Genre genre) {
        book.setGenre(genre);
        return this;
    }

    public BookBuilder withPages(Integer pages) {
        book.setPages(pages);
        return this;
    }

    public BookBuilder withPublisher(String publisher) {
        book.setPublisher(publisher);
        return this;
    }

    public BookBuilder withReleaseYear(Integer releaseYear) {
        book.setReleaseYear(releaseYear);
        return this;
    }

    public BookBuilder withOwner(User owner) {
        book.setOwner(owner);
        return this;
    }

    public BookBuilder withForSale(boolean forSale) {
        book.setForSale(forSale);
        return this;
    }

    public BookBuilder withPrice(BigDecimal price) {
        book.setPrice(price);
        return this;
    }

    public BookBuilder withOffers(Set<Offer> offers) {
        book.setOffers(offers);
        return this;
    }

    public static BookBuilder getDefaultBook() {
        BookBuilder bookBuilder = new BookBuilder();
        bookBuilder.book.setId(id);
        bookBuilder.book.setTitle("Book " + id);
        bookBuilder.book.setAuthor("Author " + id);
        bookBuilder.book.setGenre(GenreFactory.getDefaultGenre());
        bookBuilder.book.setPages(500);
        bookBuilder.book.setPublisher("Publisher");
        bookBuilder.book.setReleaseYear(2015);
        bookBuilder.book.setOwner(null);
        bookBuilder.book.setForSale(false);
        bookBuilder.book.setPrice(null);
        bookBuilder.book.setOffers(Collections.emptySet());
        id++;
        return bookBuilder;
    }
}