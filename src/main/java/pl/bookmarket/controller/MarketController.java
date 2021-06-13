package pl.bookmarket.controller;

import com.fasterxml.jackson.annotation.JsonView;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.bookmarket.dao.BookDao;
import pl.bookmarket.dao.MessageDao;
import pl.bookmarket.dao.OfferDao;
import pl.bookmarket.model.Book;
import pl.bookmarket.model.Message;
import pl.bookmarket.model.Offer;
import pl.bookmarket.model.User;
import pl.bookmarket.service.crud.UserService;
import pl.bookmarket.util.Views;
import pl.bookmarket.validation.exceptions.CustomException;
import pl.bookmarket.validation.exceptions.EntityNotFoundException;
import pl.bookmarket.validation.exceptions.ValidationException;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final UserService userService;
    private final BookDao bookDao;
    private final OfferDao offerDao;
    private final MessageDao messageDao;

    @Autowired
    public MarketController(UserService userService, BookDao bookDao, OfferDao offerDao, MessageDao messageDao) {
        this.userService = userService;
        this.bookDao = bookDao;
        this.offerDao = offerDao;
        this.messageDao = messageDao;
    }

    @GetMapping
    @JsonView(Views.Market.class)
    public List<Book> getBooksForSale(Authentication authentication) {
        return bookDao.getBooksForSale(authentication.getName());
    }

    @GetMapping("/offers")
    @JsonView(Views.Market.class)
    public List<Offer> getMyOffers(Authentication authentication) {
        return offerDao.getOffersByBuyerLogin(authentication.getName());
    }

    @GetMapping("/offers/book/{id}")
    @JsonView(Views.Offer.class)
    public List<Offer> getOffersForBook(@PathVariable Long id, Authentication authentication) {
        List<Book> myBooks = bookDao.getBooksByOwner_Login(authentication.getName());

        if (myBooks.stream().noneMatch(book -> book.getId().equals(id))) {
            throw new CustomException(String.format("The user %s does not own this book.", authentication.getName()),
                                      HttpStatus.FORBIDDEN);
        }

        return offerDao.getOffersByBook_Id(id);
    }

    @PostMapping("/offers")
    @ResponseStatus(HttpStatus.CREATED)
    @JsonView(Views.Market.class)
    public Offer addOffer(@Valid @RequestBody Offer offer, BindingResult result, Authentication authentication) {
        if (offer.getId() != null || offer.getBook().getId() == null) {
            throw new CustomException("Invalid ID", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (result.hasErrors()) {
            throw new ValidationException(result.getFieldErrors());
        }

        Optional<Book> book = bookDao.findById(offer.getBook().getId());

        if (!book.isPresent()) {
            throw new EntityNotFoundException(Book.class);
        }

        if (!book.get().isForSale()) {
            throw new ValidationException("book.not.for.sale");
        }

        if (book.get().getOwner().getLogin().equals(authentication.getName())) {
            throw new ValidationException("own.book.offer");
        }

        User currentUser = userService.getUserByLogin(authentication.getName());

        offer.setBuyer(currentUser);
        offer.setBook(book.get());
        offer.getComment().setSender(currentUser);
        offer.getComment().setReceiver(book.get().getOwner());

        //send message to book owner, that you made offer for his book
        //used message codes and delimiter '|' for new line
        StringJoiner sj = new StringJoiner("|");
        sj.add(String.format("{new.offer} \"%s\"", book.get().getTitle()));

        if (!offer.getComment().getText().isEmpty()) {
            sj.add(String.format("{comment}: %s", offer.getComment().getText()));
        }

        offer.getComment().setText(sj.toString());
        messageDao.save(offer.getComment());

        return offerDao.save(offer);
    }

    @DeleteMapping("/offers/{id}")
    public String deleteOffer(@PathVariable Long id, Authentication authentication) {
        if (!offerDao.existsById(id)) {
            throw new EntityNotFoundException(Offer.class);
        }

        List<Offer> userOffers = offerDao.getOffersByBuyerLogin(authentication.getName());

        if (userOffers.stream().noneMatch(offer -> offer.getId().equals(id))) {
            throw new CustomException("Invalid offer", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        offerDao.deleteById(id);

        return "{}";
    }

    @PostMapping("/offers/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptOffer(@RequestBody Offer offer, Authentication authentication) {
        if (offer.getId() == null) {
            throw new CustomException("Invalid ID", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        //fetch posted offer from DB
        Offer dbOffer =
            offerDao.findById(offer.getId()).orElseThrow(() -> new EntityNotFoundException(Offer.class));

        //check if buyer login and book title matches
        if (!offer.getBuyer().getLogin().equals(dbOffer.getBuyer().getLogin())
                || !offer.getBook().getTitle().equals(dbOffer.getBook().getTitle())) {
            throw new CustomException("Book title and/or owner mismatch", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        //bookseller = current user
        User bookSeller = userService.getUserByLogin(authentication.getName());

        //check if the current user is owner of the book and if he tries to accept his own offer for book of other user
        if (!dbOffer.getBook().getOwner().getLogin().equals(bookSeller.getLogin())
                || dbOffer.getBuyer().getLogin().equals(bookSeller.getLogin())) {
            throw new CustomException("Invalid book owner and/or buyer", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        //send messages to buyer about book purchase and to other bidders with information, that the book has been sold
        List<Message> messagesToSend = new ArrayList<>();

        Message msgToBuyer = new Message(dbOffer.getBook().getOwner(), dbOffer.getBuyer(),
                                         String.format("{book.bought}: %s", dbOffer.getBook().getTitle()));
        messagesToSend.add(msgToBuyer);

        for (Offer o : dbOffer.getBook().getOffers()) {
            if (dbOffer.getBuyer().getId().equals(o.getBuyer().getId())) {    //don't send this message to buyer
                continue;
            }
            Message msgToBidder = new Message(dbOffer.getBook().getOwner(), o.getBuyer(),
                                              String.format("{book.sold}: %s", dbOffer.getBook().getTitle()));
            messagesToSend.add(msgToBidder);
        }

        dbOffer.getBook().setOwner(dbOffer.getBuyer());
        dbOffer.getBook().setForSale(false);
        dbOffer.getBook().setPrice(null);

        bookDao.save(dbOffer.getBook());
        offerDao.deleteAllOffersForBook(dbOffer.getBook().getId());
        messageDao.saveAll(messagesToSend);
    }
}