package pl.bookmarket.service.crud;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bookmarket.dao.BookDao;
import pl.bookmarket.dao.OfferDao;
import pl.bookmarket.dao.UserDao;
import pl.bookmarket.model.Book;
import pl.bookmarket.model.Message;
import pl.bookmarket.model.Offer;
import pl.bookmarket.model.User;
import pl.bookmarket.security.authentication.AuthenticatedUser;
import pl.bookmarket.util.AuthUtils;
import pl.bookmarket.validation.exception.EntityNotFoundException;
import pl.bookmarket.validation.exception.EntityValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

@Service
@Transactional(readOnly = true)
public class MarketServiceImpl implements MarketService {
    private final UserDao userDao;
    private final BookDao bookDao;
    private final MessageService messageService;
    private final OfferDao offerDao;

    public MarketServiceImpl(UserDao userDao, BookDao bookDao, MessageService messageService, OfferDao offerDao) {
        this.userDao = userDao;
        this.bookDao = bookDao;
        this.messageService = messageService;
        this.offerDao = offerDao;
    }

    @Override
    public List<Offer> getOffersForBook(Long bookId) {
        Book book = bookDao.findById(bookId).orElseThrow(() -> new EntityNotFoundException(Book.class));
        verifyCurrentUserPermissions(book.getOwner());
        return offerDao.getOffersByBookId(bookId);
    }

    @Override
    @PreAuthorize("authentication.principal.id == #userId or hasRole('ADMIN')")
    public List<Offer> getOffersByUserId(Long userId) {
        if (!userDao.existsById(userId)) {
            throw new EntityNotFoundException(User.class);
        }
        return offerDao.getOffersByBuyerId(userId);
    }

    @Override
    public Optional<Offer> getOfferById(Long id) {
        Optional<Offer> offerOptional = offerDao.findById(id);
        offerOptional.ifPresent(offer -> verifyCurrentUserPermissions(offer.getBuyer()));
        return offerOptional;
    }

    @Override
    @Transactional
    public Offer addOffer(Offer offer) {
        AuthenticatedUser authenticatedUser = AuthUtils.getCurrentUser(AuthenticatedUser.class);

        if (offer.getBook() == null || offer.getBook().getId() == null) {
            throw new EntityValidationException("book", "book.invalid");
        }

        Book book = bookDao.findById(offer.getBook().getId())
                           .orElseThrow(() -> new EntityNotFoundException(Book.class));

        validateBook(book, authenticatedUser);

        User currentUser = userDao.findById(authenticatedUser.getId()).orElseThrow(NoSuchElementException::new);
        offer.setBuyer(currentUser);
        offer.setBook(book);

        //send message to book owner, that a new offer for his book has been made
        Message message = new Message(currentUser, book.getOwner(), String.format("{new.offer} \"%s\"", book.getTitle()));
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

        verifyCurrentUserPermissions(book.getOwner());

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

        bookDao.save(book);
        offerDao.deleteAllOffersForBook(book.getId());
        messageService.createMultipleMessages(messages);
    }

    @Override
    @Transactional
    public void deleteOffer(Long id) {
        Offer offer = offerDao.findById(id).orElseThrow(() -> new EntityNotFoundException(Offer.class));
        verifyCurrentUserPermissions(offer.getBuyer());

        offerDao.delete(offer);
    }

    private void verifyCurrentUserPermissions(User entityOwner) {
        Predicate<AuthenticatedUser> accessPredicate = user -> user.getId()
                                                                   .equals(entityOwner.getId()) || AuthUtils.isAdmin(user);

        if (!AuthUtils.hasAccess(AuthenticatedUser.class, accessPredicate)) {
            throw new AccessDeniedException("The current user cannot perform this action.");
        }
    }

    private void validateBook(Book book, AuthenticatedUser authenticatedUser) {
        if (!book.isForSale()) {
            throw new EntityValidationException("book", "book.not.for.sale");
        }

        if (book.getOwner().getId().equals(authenticatedUser.getId())) {
            throw new EntityValidationException("book.owner", "own.book.offer");
        }

        if (book.getOffers().stream()
                .anyMatch(bookOffer -> bookOffer.getBuyer().getId().equals(authenticatedUser.getId()))) {
            throw new EntityValidationException("offer.buyer", "offer.exists.for.user");
        }
    }
}