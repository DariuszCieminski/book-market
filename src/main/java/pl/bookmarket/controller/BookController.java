package pl.bookmarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.bookmarket.dto.BookCreateDto;
import pl.bookmarket.dto.BookDto;
import pl.bookmarket.mapper.BookMapper;
import pl.bookmarket.model.Book;
import pl.bookmarket.service.crud.BookService;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class BookController {

    private final BookService bookService;
    private final BookMapper bookMapper;

    public BookController(BookService bookService, BookMapper bookMapper) {
        this.bookService = bookService;
        this.bookMapper = bookMapper;
    }

    @GetMapping("${bm.controllers.book}")
    public List<BookDto> getAllBooks() {
        return bookService.getAllBooks().stream().map(bookMapper::bookToBookDto).collect(Collectors.toList());
    }

    @GetMapping("${bm.controllers.book}/{id}")
    public BookDto getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id).orElseThrow(() -> new EntityNotFoundException(Book.class));
        return bookMapper.bookToBookDto(book);
    }

    @GetMapping("${bm.controllers.user}/{id}/books")
    public List<BookDto> getBooksByUserId(@PathVariable Long id) {
        return bookService.getBooksByOwner(id).stream().map(bookMapper::bookToBookDto).collect(Collectors.toList());
    }

    @PostMapping("${bm.controllers.book}")
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto addBook(@Valid @RequestBody BookCreateDto book) {
        Book created = bookService.createBook(bookMapper.bookCreateDtoToBook(book));
        return bookMapper.bookToBookDto(created);
    }

    @PutMapping("${bm.controllers.book}/{id}")
    public BookDto updateBook(@Valid @RequestBody BookCreateDto book, @PathVariable Long id) {
        Book toBeUpdated = bookMapper.bookCreateDtoToBook(book);
        toBeUpdated.setId(id);
        return bookMapper.bookToBookDto(bookService.updateBook(toBeUpdated));
    }

    @DeleteMapping("${bm.controllers.book}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }
}