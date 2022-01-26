package pl.bookmarket.service.crud;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import pl.bookmarket.dao.BookDao;
import pl.bookmarket.dao.OfferDao;
import pl.bookmarket.model.Book;
import pl.bookmarket.model.User;
import pl.bookmarket.service.authentication.AuthenticatedUser;
import pl.bookmarket.util.AuthUtils;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService {
    private final BookDao bookDao;
    private final OfferDao offerDao;

    public BookServiceImpl(BookDao bookDao, OfferDao offerDao) {
        this.bookDao = bookDao;
        this.offerDao = offerDao;
    }

    @Override
    public List<Book> getBooksByOwner(Long id) {
        verifyUserPermissions(id);
        return bookDao.getBooksByOwnerId(id);
    }

    @Override
    public List<Book> getAllBooks() {
        List<Book> bookList = new ArrayList<>();
        bookDao.findAll().forEach(bookList::add);
        return bookList;
    }

    @Override
    public List<Book> getBooksForSale() {
        AuthenticatedUser currentUser = AuthUtils.getAuthenticatedUser();
        return bookDao.getBooksForSale(currentUser.getId());
    }

    @Override
    public Optional<Book> getBookById(Long id) {
        Optional<Book> bookOptional = bookDao.findById(id);
        bookOptional.ifPresent(book -> verifyUserPermissions(book.getOwner().getId()));
        return bookOptional;
    }

    @Override
    public Book createBook(Book book) {
        User currentUser = new User();
        currentUser.setId(AuthUtils.getAuthenticatedUser().getId());
        book.setOwner(currentUser);
        return bookDao.save(book);
    }

    @Override
    public Book updateBook(Book book) {
        Book bookById = bookDao.findById(book.getId()).orElseThrow(() -> new EntityNotFoundException(Book.class));
        verifyUserPermissions(bookById.getOwner().getId());
        book.setOwner(bookById.getOwner());

        // delete all offers for book if its status was changed to "not for sale"
        if (!book.isForSale() && bookById.isForSale()) {
            offerDao.deleteAllOffersForBook(bookById.getId());
        }
        return bookDao.save(book);
    }

    @Override
    public void deleteBook(Long id) {
        Book bookById = bookDao.findById(id).orElseThrow(() -> new EntityNotFoundException(Book.class));
        verifyUserPermissions(bookById.getOwner().getId());
        bookDao.delete(bookById);
    }

    private void verifyUserPermissions(Long userId) {
        AuthenticatedUser authenticatedUser = AuthUtils.getAuthenticatedUser();
        boolean validUser = userId.equals(authenticatedUser.getId());

        if (!validUser && authenticatedUser.getAuthorities().size() <= 1) {
            throw new AccessDeniedException("This book is owned by another user.");
        }
    }
}