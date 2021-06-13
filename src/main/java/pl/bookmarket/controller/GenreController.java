package pl.bookmarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.bookmarket.model.Genre;
import pl.bookmarket.service.crud.GenreService;
import pl.bookmarket.validation.exceptions.CustomException;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
public class GenreController {
    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public List<Genre> getGenres() {
        return genreService.getAllGenres();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Genre createGenre(@RequestBody Genre genre) {
        return genreService.createGenre(genre);
    }

    @PutMapping("/{id}")
    public Genre updateGenre(@PathVariable Long id, @RequestBody Genre genre) {
        if (id == null) {
            throw new CustomException("Invalid ID", HttpStatus.BAD_REQUEST);
        } else if (!id.equals(genre.getId())) {
            throw new CustomException("ID mismatched", HttpStatus.BAD_REQUEST);
        }
        return genreService.updateGenre(genre);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGenre(@PathVariable Long id) {
        genreService.deleteGenre(id);
    }
}