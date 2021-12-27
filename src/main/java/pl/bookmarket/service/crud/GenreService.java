package pl.bookmarket.service.crud;

import pl.bookmarket.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreService {
    List<Genre> getAllGenres();

    Optional<Genre> getGenreByName(String name);

    Genre createGenre(Genre genre);

    Genre updateGenre(Genre genre);

    void deleteGenre(Long id);
}