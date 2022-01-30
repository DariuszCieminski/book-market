package pl.bookmarket.mapper;

import org.mapstruct.Mapper;
import pl.bookmarket.dto.BookCreateDto;
import pl.bookmarket.dto.BookDto;
import pl.bookmarket.model.Book;

@Mapper(uses = {GenreMapper.class, UserMapper.class})
public interface BookMapper {

    BookDto bookToBookDto(Book book);

    Book bookCreateDtoToBook(BookCreateDto bookCreateDto);
}