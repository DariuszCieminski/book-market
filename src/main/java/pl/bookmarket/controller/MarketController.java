package pl.bookmarket.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.bookmarket.model.Book;
import pl.bookmarket.model.Offer;
import pl.bookmarket.service.crud.BookService;
import pl.bookmarket.service.crud.MarketService;
import pl.bookmarket.util.Views;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketService marketService;
    private final BookService bookService;

    @Autowired
    public MarketController(MarketService marketService, BookService bookService) {
        this.marketService = marketService;
        this.bookService = bookService;
    }

    @GetMapping
    @JsonView(Views.Market.class)
    public List<Book> getBooksForSale() {
        return bookService.getBooksForSale();
    }

    @GetMapping("/offers")
    @JsonView(Views.Market.class)
    public List<Offer> getMyOffers(Authentication authentication) {
        return marketService.getOffersByUserLogin(authentication.getName());
    }

    @GetMapping("/offers/book/{id}")
    @JsonView(Views.Offer.class)
    public List<Offer> getOffersForBook(@PathVariable Long id) {
        return marketService.getOffersForBook(id);
    }

    @PostMapping("/offers")
    @ResponseStatus(HttpStatus.CREATED)
    @JsonView(Views.Market.class)
    public Offer addOffer(@Valid @RequestBody Offer offer) {
        return marketService.addOffer(offer);
    }

    @PostMapping("/offers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptOffer(@PathVariable Long id) {
        marketService.acceptOffer(id);
    }

    @DeleteMapping("/offers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOffer(@PathVariable Long id) {
        marketService.deleteOffer(id);
    }
}