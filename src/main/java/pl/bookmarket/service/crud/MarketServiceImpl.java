package pl.bookmarket.service.crud;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bookmarket.dao.OfferDao;
import pl.bookmarket.model.Book;
import pl.bookmarket.model.Message;
import pl.bookmarket.model.Offer;
import pl.bookmarket.model.User;
import pl.bookmarket.security.authentication.AuthenticatedUser;
import pl.bookmarket.util.AuthUtils;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;
import pl.bookmarket.validation.exceptions.EntityValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Predicate;

@Service
@Transactional(readOnly = true)
public class MarketServiceImpl implements MarketService {
    private final UserService userService;
    private final BookService bookService;
    private final MessageService messageService;
    private final OfferDao offerDao;

    public MarketServiceImpl(UserService userService, BookService bookService, MessageService messageService, OfferDao offerDao) {
        this.userService = userService;
        this.bookService = bookService;
        this.messageService = messageService;
        this.offerDao = offerDao;
    }

    @Override
    public List<Offer> getOffersForBook(Long bookId) {
        Book book = bookService.getBookById(bookId).orElseThrow(() -> new EntityNotFoundException(Book.class));
        verifyUserPermissions(book);
        return offerDao.getOffersByBookId(bookId);
    }

    @Override
    @PreAuthorize("authentication.principal.id == #userId or hasRole('ADMIN')")
    public List<Offer> getOffersByUserId(Long userId) {
        return offerDao.getOffersByBuyerId(userId);
    }

    @Override
    public Optional<Offer> getOfferById(Long id) {
        Optional<Offer> offerOptional = offerDao.findById(id);
        offerOptional.ifPresent(this::verifyUserPermissions);
        return offerOptional;
    }

    @Override
    @Transactional
    public Offer addOffer(Offer offer) {
        AuthenticatedUser authenticatedUser = AuthUtils.getCurrentUser(AuthenticatedUser.class);

        if (offer.getBook() == null || offer.getBook().getId() == null) {
            throw new EntityValidationException("book", "book.invalid");
        }

        Book book = bookService.getBookById(offer.getBook().getId())
                               .orElseThrow(() -> new EntityNotFoundException(Book.class));

        if (!book.isForSale()) {
            throw new EntityValidationException("book", "book.not.for.sale");
        }

        if (book.getOwner().getId().equals(authenticatedUser.getId())) {
            throw new EntityValidationException("book.owner", "own.book.offer");
        }

        User currentUser = userService.getUserById(authenticatedUser.getId())
                                      .orElseThrow(NoSuchElementException::new);
        offer.setBuyer(currentUser);
        offer.setBook(book);

        //send message to book owner, that a new offer for his book has been made
        //used message codes and delimiter '|' for new line
        StringJoiner sj = new StringJoiner("|");
        sj.add(String.format("{new.offer} \"%s\"", book.getTitle()));

        if (!offer.getComment().isEmpty()) {
            sj.add(String.format("{comment}: %s", offer.getComment()));
        }

        Message message = new Message(currentUser, book.getOwner(), sj.toString());
        messageService.createMessage(message);

        return offerDao.save(offer);
    }

    @Override
    @Transactional
    public void acceptOffer(Long id) {
        Offer offer = offerDao.findById(id).orElseThrow(() -> new EntityNotFoundException(Offer.class));
        Book book = offer.getBook();
        User buyer = offer.getBuyer();
        User seller = book.getOwner();

        verifyUserPermissions(book);

        List<Message> messages = new ArrayList<>();
        messages.add(new Message(seller, buyer, "{book.bought}: " + book.getTitle()));

        for (Offer o : book.getOffers()) {
            if (!o.getId().equals(offer.getId())) {
                messages.add(new Message(seller, o.getBuyer(), "{book.sold}: " + book.getTitle()));
            }
        }

        book.setOwner(buyer);
        book.setForSale(false);
        book.setPrice(null);

        bookService.updateBook(book);
        offerDao.deleteAllOffersForBook(book.getId());
        messageService.createMultipleMessages(messages);
    }

    @Override
    @Transactional
    public void deleteOffer(Long id) {
        Offer offer = offerDao.findById(id).orElseThrow(() -> new EntityNotFoundException(Offer.class));
        verifyUserPermissions(offer);

        offerDao.delete(offer);
    }

    private void verifyUserPermissions(Book book) {
        Predicate<AuthenticatedUser> accessPredicate = user -> book.getOwner().getId()
                                                                   .equals(user.getId()) || AuthUtils.isAdmin(user);

        if (!AuthUtils.hasAccess(AuthenticatedUser.class, accessPredicate)) {
            throw new AccessDeniedException("This book is owned by another user.");
        }
    }

    private void verifyUserPermissions(Offer offer) {
        Predicate<AuthenticatedUser> predicate = user -> offer.getBuyer().getId()
                                                              .equals(user.getId()) || AuthUtils.isAdmin(user);

        if (!AuthUtils.hasAccess(AuthenticatedUser.class, predicate)) {
            throw new AccessDeniedException("This offer was made by another user.");
        }
    }
}