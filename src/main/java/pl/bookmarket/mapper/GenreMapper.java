package pl.bookmarket.mapper;

import org.mapstruct.Mapper;
import pl.bookmarket.dto.GenreDto;
import pl.bookmarket.model.Genre;

@Mapper
public interface GenreMapper {

    GenreDto genreToDto(Genre genre);

    Genre dtoToGenre(GenreDto genreDto);
}