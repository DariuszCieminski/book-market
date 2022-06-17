package pl.bookmarket.service.crud;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import pl.bookmarket.dao.GenreDao;
import pl.bookmarket.model.Genre;
import pl.bookmarket.validation.exception.EntityNotFoundException;
import pl.bookmarket.validation.exception.EntityValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GenreServiceImpl implements GenreService {
    private final GenreDao genreDao;

    public GenreServiceImpl(GenreDao genreDao) {
        this.genreDao = genreDao;
    }

    @Override
    public List<Genre> getAllGenres() {
        List<Genre> genreList = new ArrayList<>();
        genreDao.findAll().forEach(genreList::add);
        return genreList;
    }

    @Override
    public Optional<Genre> getGenreById(Long id) {
        return genreDao.findById(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Genre createGenre(Genre genre) {
        if (genreDao.existsGenreByName(genre.getName())) {
            throw new EntityValidationException("name", "name.occupied");
        }
        return genreDao.save(genre);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Genre updateGenre(Genre genre) {
        Genre byId = genreDao.findById(genre.getId()).orElseThrow(() -> new EntityNotFoundException(Genre.class));
        if (genreDao.existsGenreByName(genre.getName()) && !genre.getName().equals(byId.getName())) {
            throw new EntityValidationException("name", "name.occupied");
        }
        return genreDao.save(genre);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteGenre(Long id) {
        if (!genreDao.existsById(id)) {
            throw new EntityNotFoundException(Genre.class);
        }
        genreDao.deleteById(id);
    }
}