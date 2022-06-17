package pl.bookmarket.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.bookmarket.model.Genre;

@Repository
public interface GenreDao extends CrudRepository<Genre, Long> {

    boolean existsGenreByName(String name);
}