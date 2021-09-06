package pl.bookmarket.service.crud;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.bookmarket.dao.BookDao;
import pl.bookmarket.model.Book;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;
import pl.bookmarket.validation.exceptions.EntityValidationException;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookServiceImpl implements BookService {
    private final BookDao bookDao;
    private final UserService userService;

    public BookServiceImpl(BookDao bookDao, UserService userService) {
        this.bookDao = bookDao;
        this.userService = userService;
    }

    @Override
    public List<Book> getBooksByOwnerLogin(String login) {
        return bookDao.getBooksByOwnerLogin(login);
    }

    @Override
    public List<Book> getAllBooks() {
        List<Book> bookList = new ArrayList<>();
        bookDao.findAll().forEach(bookList::add);
        return bookList;
    }

    @Override
    public List<Book> getBooksForSale() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return bookDao.getBooksForSale(authentication.getName());
    }

    @Override
    public Book getBookById(Long id) {
        return bookDao.findById(id).orElseThrow(() -> new EntityNotFoundException(Book.class));
    }

    @Override
    public Book createBook(Book book) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        book.setOwner(userService.getUserByLogin(authentication.getName()));
        return bookDao.save(book);
    }

    @Override
    public Book updateBook(Book book) {
        Book b = bookDao.findById(book.getId()).orElseThrow(() -> new EntityNotFoundException(Book.class));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!b.getOwner().getLogin().equals(authentication.getName())) {
            throw new EntityValidationException("id", "not.users.book");
        }
        return bookDao.save(book);
    }

    @Override
    public void deleteBook(Long id) {
        Book b = bookDao.findById(id).orElseThrow(() -> new EntityNotFoundException(Book.class));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!b.getOwner().getLogin().equals(authentication.getName())) {
            throw new EntityValidationException("id", "not.users.book");
        }
        bookDao.deleteById(id);
    }
}