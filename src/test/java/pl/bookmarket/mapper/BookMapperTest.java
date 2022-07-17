package pl.bookmarket.mapper;

import org.junit.jupiter.api.Test;
import pl.bookmarket.dto.BookCreateDto;
import pl.bookmarket.dto.BookDto;
import pl.bookmarket.model.Book;
import pl.bookmarket.testhelpers.datafactory.BookBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookMapperTest {

    private final BookMapper mapper = new BookMapperImpl(new GenreMapperImpl(), new UserMapperImpl(new RoleMapperImpl()));

    @Test
    void bookToBookDto() {
        Book book = BookBuilder.getDefaultBook().build();

        BookDto bookDto = mapper.bookToBookDto(book);

        assertEquals(book.getId(), bookDto.getId());
        assertEquals(book.getTitle(), bookDto.getTitle());
        assertEquals(book.getAuthor(), bookDto.getAuthor());
        assertEquals(book.getGenre().getId(), bookDto.getGenre().getId());
        assertEquals(book.getGenre().getName(), bookDto.getGenre().getName());
        assertEquals(book.getPages(), bookDto.getPages());
        assertEquals(book.getPublisher(), bookDto.getPublisher());
        assertEquals(book.getReleaseYear(), bookDto.getReleaseYear());
        assertEquals(book.getOwner().getId(), bookDto.getOwner().getId());
        assertEquals(book.getOwner().getLogin(), bookDto.getOwner().getLogin());
        assertEquals(book.isForSale(), bookDto.isForSale());
        assertEquals(book.getPrice(), bookDto.getPrice());
    }

    @Test
    void bookCreateDtoToBook() {
        BookCreateDto bookCreateDto = BookBuilder.getDefaultBook().buildBookCreateDto();

        Book book = mapper.bookCreateDtoToBook(bookCreateDto);

        assertEquals(bookCreateDto.getTitle(), book.getTitle());
        assertEquals(bookCreateDto.getAuthor(), book.getAuthor());
        assertEquals(bookCreateDto.getGenreId(), book.getGenre().getId());
        assertEquals(bookCreateDto.getPages(), book.getPages());
        assertEquals(bookCreateDto.getPublisher(), book.getPublisher());
        assertEquals(bookCreateDto.getReleaseYear(), book.getReleaseYear());
        assertEquals(bookCreateDto.isForSale(), book.isForSale());
        assertEquals(bookCreateDto.getPrice(), book.getPrice());
    }
}