package pl.bookmarket.service.crud;

import org.springframework.stereotype.Service;
import pl.bookmarket.dao.GenreDao;
import pl.bookmarket.model.Genre;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;

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
    public Genre getGenreByName(String name) {
        return genreDao.findGenreByName(name).orElseThrow(() -> new EntityNotFoundException(Genre.class));
    }

    @Override
    public Genre createGenre(Genre genre) {
        return genreDao.save(genre);
    }

    @Override
    public Genre updateGenre(Genre genre) {
        if (!genreDao.existsById(genre.getId())) {
            throw new EntityNotFoundException(Genre.class);
        }
        return genreDao.save(genre);
    }

    @Override
    public void deleteGenre(Long id) {
        if (!genreDao.existsById(id)) {
            throw new EntityNotFoundException(Genre.class);
        }
        genreDao.deleteById(id);
    }
}