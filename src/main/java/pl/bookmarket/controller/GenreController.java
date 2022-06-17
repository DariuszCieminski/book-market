package pl.bookmarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.bookmarket.dto.GenreDto;
import pl.bookmarket.mapper.GenreMapper;
import pl.bookmarket.model.Genre;
import pl.bookmarket.service.crud.GenreService;
import pl.bookmarket.validation.exception.EntityNotFoundException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${bm.controllers.genre}")
public class GenreController {

    private final GenreService genreService;
    private final GenreMapper genreMapper;

    public GenreController(GenreService genreService, GenreMapper genreMapper) {
        this.genreService = genreService;
        this.genreMapper = genreMapper;
    }

    @GetMapping
    public List<GenreDto> getGenres() {
        return genreService.getAllGenres().stream().map(genreMapper::genreToDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public GenreDto getGenreById(@PathVariable Long id) {
        Genre genre = genreService.getGenreById(id).orElseThrow(() -> new EntityNotFoundException(Genre.class));
        return genreMapper.genreToDto(genre);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GenreDto createGenre(@Valid @RequestBody GenreDto genre) {
        Genre created = genreService.createGenre(genreMapper.dtoToGenre(genre));
        return genreMapper.genreToDto(created);
    }

    @PutMapping("/{id}")
    public GenreDto updateGenre(@Valid @RequestBody GenreDto genre, @PathVariable Long id) {
        Genre toBeUpdated = genreMapper.dtoToGenre(genre);
        toBeUpdated.setId(id);
        return genreMapper.genreToDto(genreService.updateGenre(toBeUpdated));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGenre(@PathVariable Long id) {
        genreService.deleteGenre(id);
    }
}