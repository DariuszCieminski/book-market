package pl.bookmarket.dao;

import org.springframework.data.repository.CrudRepository;
import pl.bookmarket.model.Genre;

public interface GenreDao extends CrudRepository<Genre, Long> {

    Genre findGenreByName(String name);
}