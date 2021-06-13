package pl.bookmarket.validation.constraints;

import org.springframework.beans.factory.annotation.Autowired;
import pl.bookmarket.dao.GenreDao;
import pl.bookmarket.model.Genre;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueGenreValidator implements ConstraintValidator<UniqueGenre, Genre> {

    private final GenreDao genreDao;

    @Autowired
    public UniqueGenreValidator(GenreDao genreDao) {
        this.genreDao = genreDao;
    }

    @Override
    public boolean isValid(Genre value, ConstraintValidatorContext context) {
        Optional<Genre> dbGenre = genreDao.findGenreByName(value.getName());

        boolean valid = (!dbGenre.isPresent() || dbGenre.get().getId().equals(value.getId()));

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{genre.name.occupied}")
                   .addPropertyNode("name")
                   .addConstraintViolation();
        }

        return valid;
    }
}