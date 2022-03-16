package pl.bookmarket.service.crud;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bookmarket.dao.BookDao;
import pl.bookmarket.dao.OfferDao;
import pl.bookmarket.model.Book;
import pl.bookmarket.model.User;
import pl.bookmarket.security.authentication.AuthenticatedUser;
import pl.bookmarket.util.AuthUtils;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class BookServiceImpl implements BookService {
    private final BookDao bookDao;
    private final OfferDao offerDao;

    public BookServiceImpl(BookDao bookDao, OfferDao offerDao) {
        this.bookDao = bookDao;
        this.offerDao = offerDao;
    }

    @Override
    @PreAuthorize("authentication.principal.id == #id or hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<Book> getBooksByOwner(Long id) {
        return bookDao.getBooksByOwnerId(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<Book> getAllBooks() {
        List<Book> bookList = new ArrayList<>();
        bookDao.findAll().forEach(bookList::add);
        return bookList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> getBooksForSale() {
        AuthenticatedUser currentUser = AuthUtils.getCurrentUser(AuthenticatedUser.class);
        return bookDao.getBooksForSale(currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Book> getBookById(Long id) {
        Optional<Book> bookOptional = bookDao.findById(id);
        bookOptional.ifPresent(this::verifyUserPermissions);
        return bookOptional;
    }

    @Override
    public Book createBook(Book book) {
        User currentUser = new User();
        currentUser.setId(AuthUtils.getCurrentUser(AuthenticatedUser.class).getId());
        book.setOwner(currentUser);
        return bookDao.save(book);
    }

    @Override
    @Transactional
    public Book updateBook(Book book) {
        Book bookById = bookDao.findById(book.getId()).orElseThrow(() -> new EntityNotFoundException(Book.class));
        verifyUserPermissions(bookById);
        book.setOwner(bookById.getOwner());

        // delete all offers for book if its status was changed to "not for sale"
        if (!book.isForSale() && bookById.isForSale()) {
            offerDao.deleteAllOffersForBook(bookById.getId());
        }
        return bookDao.save(book);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book bookById = bookDao.findById(id).orElseThrow(() -> new EntityNotFoundException(Book.class));
        verifyUserPermissions(bookById);
        bookDao.delete(bookById);
    }

    private void verifyUserPermissions(Book book) {
        Predicate<AuthenticatedUser> predicate = user -> book.getOwner().getId()
                                                             .equals(user.getId()) || AuthUtils.isAdmin(user);

        if (!AuthUtils.hasAccess(AuthenticatedUser.class, predicate)) {
            throw new AccessDeniedException("This book is owned by another user.");
        }
    }
}