package pl.bookmarket.validation.constraints;

import org.springframework.beans.factory.annotation.Autowired;
import pl.bookmarket.dao.GenreDao;
import pl.bookmarket.dto.GenreDto;
import pl.bookmarket.model.Genre;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class UniqueGenreValidator implements ConstraintValidator<UniqueGenre, GenreDto> {

    private final GenreDao genreDao;

    @Autowired
    public UniqueGenreValidator(GenreDao genreDao) {
        this.genreDao = genreDao;
    }

    @Override
    public boolean isValid(GenreDto value, ConstraintValidatorContext context) {
        Optional<Genre> genre = genreDao.findGenreByName(value.getName());

        boolean valid = (!genre.isPresent() || genre.get().getId().equals(value.getId()));

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("name.occupied")
                   .addPropertyNode("name")
                   .addConstraintViolation();
        }

        return valid;
    }
}