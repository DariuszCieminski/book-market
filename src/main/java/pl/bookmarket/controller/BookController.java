package pl.bookmarket.controller;

import com.fasterxml.jackson.annotation.JsonView;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.bookmarket.dao.BookDao;
import pl.bookmarket.dao.GenreDao;
import pl.bookmarket.dao.OfferDao;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.model.Book;
import pl.bookmarket.model.Genre;
import pl.bookmarket.model.User;
import pl.bookmarket.util.Views;
import pl.bookmarket.validation.exceptions.CustomException;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;
import pl.bookmarket.validation.exceptions.ValidationException;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookDao bookDao;
    private final UserDao userDao;
    private final GenreDao genreDao;
    private final OfferDao offerDao;

    @Autowired
    public BookController(BookDao bookDao, UserDao userDao, GenreDao genreDao, OfferDao offerDao) {
        this.bookDao = bookDao;
        this.userDao = userDao;
        this.genreDao = genreDao;
        this.offerDao = offerDao;
    }

    @GetMapping
    @JsonView(Views.Books.class)
    public List<Book> getMyBooks(Authentication authentication) {
        return bookDao.getBooksByOwner_Login(authentication.getName());
    }

    @GetMapping("/genres")
    public Iterable<Genre> getGenreList() {
        return genreDao.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @JsonView(Views.Books.class)
    public Book addBook(@Valid @RequestBody Book book, BindingResult result, Authentication authentication) {
        if (book.getId() != null) {
            throw new CustomException("Invalid ID.", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (result.hasErrors()) {
            throw new ValidationException(result.getFieldErrors());
        }

        book.setOwner(userDao.findUserByLogin(authentication.getName()));

        return bookDao.save(book);
    }

    @PutMapping("/{id}")
    @JsonView(Views.Books.class)
    public Book editBook(@Valid @RequestBody Book book, BindingResult result, @PathVariable Long id,
                         Authentication authentication) {
        if (!id.equals(book.getId())) {
            throw new CustomException("ID mismatch", HttpStatus.BAD_REQUEST);
        }

        if (result.hasErrors()) {
            throw new ValidationException(result.getFieldErrors());
        }

        if (!bookDao.existsById(id)) {
            throw new EntityNotFoundException(Book.class, id);
        }

        User currentUser = userDao.findUserByLogin(authentication.getName());

        if (currentUser.getBooks().stream().noneMatch(bk -> bk.getId().equals(id))) {
            throw new CustomException(String.format("The user %s does not own this book.", authentication.getName()),
                                      HttpStatus.FORBIDDEN);
        }

        //if we changed for sale status, delete all offers for this book
        if (!book.isForSale()) {
            offerDao.deleteAllOffersForBook(book.getId());
        }

        book.setOwner(currentUser);

        return bookDao.save(book);
    }

    @DeleteMapping("/{id}")
    public String deleteBook(@PathVariable Long id, Authentication authentication) {
        if (!bookDao.existsById(id)) {
            throw new EntityNotFoundException(Book.class, id);
        }

        List<Book> currentUserBooks = bookDao.getBooksByOwner_Login(authentication.getName());

        if (currentUserBooks.stream().noneMatch(bk -> bk.getId().equals(id))) {
            throw new CustomException(String.format("The user %s does not own this book.", authentication.getName()),
                                      HttpStatus.FORBIDDEN);
        }

        bookDao.deleteById(id);

        return "{}";
    }
}