package pl.bookmarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.bookmarket.model.Book;
import pl.bookmarket.model.Offer;
import pl.bookmarket.service.crud.BookService;
import pl.bookmarket.service.crud.MarketService;

import javax.validation.Valid;
import java.util.List;

@RestController
public class MarketController {

    private final MarketService marketService;
    private final BookService bookService;

    public MarketController(MarketService marketService, BookService bookService) {
        this.marketService = marketService;
        this.bookService = bookService;
    }

    @GetMapping("${bm.controllers.market}")
    public List<Book> getBooksForSale() {
        return bookService.getBooksForSale();
    }

    @GetMapping("${bm.controllers.user}/{id}/offers")
    public List<Offer> getOffersForUser(@PathVariable Long id) {
        return marketService.getOffersByUserId(id);
    }

    @GetMapping("${bm.controllers.book}/{id}/offers")
    public List<Offer> getOffersForBook(@PathVariable Long id) {
        return marketService.getOffersForBook(id);
    }

    @PostMapping("${bm.controllers.offer}")
    @ResponseStatus(HttpStatus.CREATED)
    public Offer addOffer(@Valid @RequestBody Offer offer) {
        return marketService.addOffer(offer);
    }

    @PostMapping("${bm.controllers.offer}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptOffer(@PathVariable Long id) {
        marketService.acceptOffer(id);
    }

    @DeleteMapping("${bm.controllers.offer}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOffer(@PathVariable Long id) {
        marketService.deleteOffer(id);
    }
}