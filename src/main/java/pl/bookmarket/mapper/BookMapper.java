package pl.bookmarket.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.bookmarket.dto.BookCreateDto;
import pl.bookmarket.dto.BookDto;
import pl.bookmarket.model.Book;

@Mapper(uses = {GenreMapper.class, UserMapper.class})
public interface BookMapper {

    BookDto bookToBookDto(Book book);

    @Mapping(source = "genreId", target = "genre.id")
    Book bookCreateDtoToBook(BookCreateDto bookCreateDto);
}