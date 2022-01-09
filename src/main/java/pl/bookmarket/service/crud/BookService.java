package pl.bookmarket.service.crud;

import pl.bookmarket.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookService {
    List<Book> getBooksByOwner(Long id);

    List<Book> getAllBooks();

    List<Book> getBooksForSale();

    Optional<Book> getBookById(Long id);

    Book createBook(Book book);

    Book updateBook(Book book);

    void deleteBook(Long id);
}