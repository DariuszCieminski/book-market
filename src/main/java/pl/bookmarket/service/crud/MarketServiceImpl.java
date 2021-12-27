package pl.bookmarket.service.crud;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.bookmarket.dao.OfferDao;
import pl.bookmarket.model.Book;
import pl.bookmarket.model.Message;
import pl.bookmarket.model.Offer;
import pl.bookmarket.model.User;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;
import pl.bookmarket.validation.exceptions.EntityValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.StringJoiner;

@Service
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Book book = bookService.getBookById(bookId).orElseThrow(() -> new EntityNotFoundException(Book.class));
        if (!book.getOwner().getLogin().equals(authentication.getName())
                && !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            throw new EntityValidationException("id", "not.users.book");
        }
        return offerDao.getOffersByBookId(bookId);
    }

    @Override
    public List<Offer> getOffersByUserLogin(String login) {
        return offerDao.getOffersByBuyerLogin(login);
    }

    @Override
    public Optional<Offer> getOfferById(Long id) {
        return offerDao.findById(id);
    }

    @Override
    public Offer addOffer(Offer offer) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (offer.getBook() == null || offer.getBook().getId() == null) {
            throw new EntityValidationException("book", "book.invalid");
        }

        Book book = bookService.getBookById(offer.getBook().getId())
                               .orElseThrow(() -> new EntityNotFoundException(Book.class));

        if (!book.isForSale()) {
            throw new EntityValidationException("book", "book.not.for.sale");
        }

        if (book.getOwner().getLogin().equals(authentication.getName())) {
            throw new EntityValidationException("book.owner", "own.book.offer");
        }

        User currentUser = userService.getUserByLogin(authentication.getName())
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
    public void acceptOffer(Long id) {
        Offer offer = offerDao.findById(id).orElseThrow(() -> new EntityNotFoundException(Offer.class));
        Book book = offer.getBook();
        User buyer = offer.getBuyer();
        User seller = book.getOwner();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!seller.getLogin().equals(authentication.getName())) {
            throw new EntityValidationException("id", "accept.invalid.offer");
        }

        List<Message> messages = new ArrayList<>();
        messages.add(new Message(seller, buyer, "{book.bought}: " + book.getTitle()));

        for (Offer o : book.getOffers()) {
            if (o.getId().equals(offer.getId())) continue;
            messages.add(new Message(seller, o.getBuyer(), "{book.sold}: " + book.getTitle()));
        }

        book.setOwner(buyer);
        book.setForSale(false);
        book.setPrice(null);

        bookService.updateBook(book);
        offerDao.deleteAllOffersForBook(book.getId());
        messageService.createMultipleMessages(messages);
    }

    @Override
    public void deleteOffer(Long id) {
        Offer offer = offerDao.findById(id).orElseThrow(() -> new EntityNotFoundException(Offer.class));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!offer.getBuyer().getLogin().equals(authentication.getName())) {
            throw new EntityValidationException("id", "not.users.offer");
        }

        offerDao.delete(offer);
    }
}