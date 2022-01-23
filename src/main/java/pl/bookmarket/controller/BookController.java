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
import pl.bookmarket.model.Book;
import pl.bookmarket.service.crud.BookService;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;

import javax.validation.Valid;
import java.util.List;

@RestController
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("${bm.controllers.book}")
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("${bm.controllers.book}/{id}")
    public Book getBookById(@PathVariable Long id) {
        return bookService.getBookById(id).orElseThrow(() -> new EntityNotFoundException(Book.class));
    }

    @GetMapping("${bm.controllers.user}/{id}/books")
    public List<Book> getBooksByUserId(@PathVariable Long id) {
        return bookService.getBooksByOwner(id);
    }

    @PostMapping("${bm.controllers.book}")
    @ResponseStatus(HttpStatus.CREATED)
    public Book addBook(@Valid @RequestBody Book book) {
        return bookService.createBook(book);
    }

    @PutMapping("${bm.controllers.book}/{id}")
    public Book updateBook(@Valid @RequestBody Book book, @PathVariable Long id) {
        return bookService.updateBook(book);
    }

    @DeleteMapping("${bm.controllers.book}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }
}