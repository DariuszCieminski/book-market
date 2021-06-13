package pl.bookmarket.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.bookmarket.model.Genre;

import java.util.Optional;

@Repository
public interface GenreDao extends CrudRepository<Genre, Long> {

    Optional<Genre> findGenreByName(String name);
}