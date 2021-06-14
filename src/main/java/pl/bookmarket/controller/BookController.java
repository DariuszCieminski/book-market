package pl.bookmarket.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.bookmarket.model.Book;
import pl.bookmarket.service.crud.BookService;
import pl.bookmarket.util.Views;
import pl.bookmarket.validation.exceptions.CustomException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    @JsonView(Views.Books.class)
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/{id}")
    @JsonView(Views.Books.class)
    public Book getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    @GetMapping("/my-books")
    @JsonView(Views.Books.class)
    public List<Book> getMyBooks(Authentication authentication) {
        return bookService.getBooksByOwnerLogin(authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @JsonView(Views.Books.class)
    public Book addBook(@Valid @RequestBody Book book) {
        if (book.getId() != null) {
            throw new CustomException("Book must not have an ID.", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return bookService.createBook(book);
    }

    @PutMapping("/{id}")
    @JsonView(Views.Books.class)
    public Book editBook(@Valid @RequestBody Book book, @PathVariable Long id) {
        if (id == null) {
            throw new CustomException("Invalid ID", HttpStatus.BAD_REQUEST);
        } else if (!id.equals(book.getId())) {
            throw new CustomException("ID mismatched", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return bookService.updateBook(book);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }
}