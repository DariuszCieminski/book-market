package pl.bookmarket.mapper;

import org.junit.jupiter.api.Test;
import pl.bookmarket.dto.GenreDto;
import pl.bookmarket.model.Genre;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenreMapperTest {

    private final GenreMapper mapper = new GenreMapperImpl();

    @Test
    void genreToDto() {
        Genre genre = new Genre(123L, "TestGenre123");

        GenreDto genreDto = mapper.genreToDto(genre);

        assertEquals(genre.getId(), genreDto.getId());
        assertEquals(genre.getName(), genreDto.getName());
    }

    @Test
    void dtoToGenre() {
        GenreDto genreDto = new GenreDto();
        genreDto.setId(333L);
        genreDto.setName("GenreDto333");

        Genre genre = mapper.dtoToGenre(genreDto);

        assertEquals(genreDto.getId(), genre.getId());
        assertEquals(genreDto.getName(), genre.getName());
    }
}