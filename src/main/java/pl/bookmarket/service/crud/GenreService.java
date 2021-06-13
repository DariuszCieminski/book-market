package pl.bookmarket.service.crud;

import pl.bookmarket.model.Genre;

import java.util.List;

public interface GenreService {
    List<Genre> getAllGenres();

    Genre getGenreByName(String name);

    Genre createGenre(Genre genre);

    Genre updateGenre(Genre genre);

    void deleteGenre(Long id);
}