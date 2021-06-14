package pl.bookmarket.service.crud;

import pl.bookmarket.model.Book;

import java.util.List;

public interface BookService {
    List<Book> getBooksByOwnerLogin(String login);

    List<Book> getAllBooks();

    List<Book> getBooksForSale();

    Book getBookById(Long id);

    Book createBook(Book book);

    Book updateBook(Book book);

    void deleteBook(Long id);
}