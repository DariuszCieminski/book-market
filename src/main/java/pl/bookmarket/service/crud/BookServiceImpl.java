package pl.bookmarket.service.crud;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.bookmarket.dao.BookDao;
import pl.bookmarket.model.Book;
import pl.bookmarket.model.User;
import pl.bookmarket.validation.exceptions.CustomException;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;

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

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AuthenticationCredentialsNotFoundException("Authentication not found.");
        }
        return userService.getUserByLogin(authentication.getName());
    }

    @Override
    public List<Book> getBooksByOwnerLogin(String login) {
        return bookDao.getBooksByOwner_Login(login);
    }

    @Override
    public List<Book> getAllBooks() {
        List<Book> bookList = new ArrayList<>();
        bookDao.findAll().forEach(bookList::add);
        return bookList;
    }

    @Override
    public List<Book> getBooksForSale() {
        return bookDao.getBooksForSale(getCurrentUser().getLogin());
    }

    @Override
    public Book getBookById(Long id) {
        return bookDao.findById(id).orElseThrow(() -> new EntityNotFoundException(Book.class));
    }

    @Override
    public Book createBook(Book book) {
        book.setOwner(getCurrentUser());
        return bookDao.save(book);
    }

    @Override
    public Book updateBook(Book book) {
        if (!bookDao.existsById(book.getId())) {
            throw new EntityNotFoundException(Book.class);
        }
        User currentUser = getCurrentUser();
        if (currentUser.getBooks().stream().noneMatch(b -> b.getId().equals(book.getId()))) {
            throw new CustomException(String.format("The user %s does not own this book.", currentUser.getLogin()),
                                      HttpStatus.FORBIDDEN);
        }
        return bookDao.save(book);
    }

    @Override
    public void deleteBook(Long id) {
        if (!bookDao.existsById(id)) {
            throw new EntityNotFoundException(Book.class);
        }
        User currentUser = getCurrentUser();
        if (currentUser.getBooks().stream().noneMatch(b -> b.getId().equals(id))) {
            throw new CustomException(String.format("The user %s does not own this book.", currentUser.getLogin()),
                                      HttpStatus.FORBIDDEN);
        }
        bookDao.deleteById(id);
    }
}