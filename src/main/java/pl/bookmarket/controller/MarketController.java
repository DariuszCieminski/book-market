package pl.bookmarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.bookmarket.dto.BookDto;
import pl.bookmarket.dto.OfferCreateDto;
import pl.bookmarket.dto.OfferDto;
import pl.bookmarket.mapper.BookMapper;
import pl.bookmarket.mapper.OfferMapper;
import pl.bookmarket.model.Offer;
import pl.bookmarket.service.crud.BookService;
import pl.bookmarket.service.crud.MarketService;
import pl.bookmarket.validation.exception.EntityNotFoundException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MarketController {

    private final MarketService marketService;
    private final BookService bookService;
    private final OfferMapper offerMapper;
    private final BookMapper bookMapper;

    public MarketController(MarketService marketService, BookService bookService, OfferMapper offerMapper, BookMapper bookMapper) {
        this.marketService = marketService;
        this.bookService = bookService;
        this.offerMapper = offerMapper;
        this.bookMapper = bookMapper;
    }

    @GetMapping("${bm.controllers.book}/forsale")
    public List<BookDto> getBooksForSale() {
        return bookService.getBooksForSale().stream().map(bookMapper::bookToBookDto)
                          .collect(Collectors.toList());
    }

    @GetMapping("${bm.controllers.user}/{id}/offers")
    public List<OfferDto> getOffersForUser(@PathVariable Long id) {
        return marketService.getOffersByUserId(id).stream().map(offerMapper::offerToOfferDto)
                            .collect(Collectors.toList());
    }

    @GetMapping("${bm.controllers.book}/{id}/offers")
    public List<OfferDto> getOffersForBook(@PathVariable Long id) {
        return marketService.getOffersForBook(id).stream().map(offerMapper::offerToOfferDto)
                            .collect(Collectors.toList());
    }

    @GetMapping("${bm.controllers.offer}/{id}")
    public OfferDto getOfferById(@PathVariable Long id) {
        return offerMapper.offerToOfferDto(marketService.getOfferById(id)
                                                        .orElseThrow(() -> new EntityNotFoundException(Offer.class)));
    }

    @PostMapping("${bm.controllers.offer}")
    @ResponseStatus(HttpStatus.CREATED)
    public OfferDto addOffer(@Valid @RequestBody OfferCreateDto offer) {
        Offer created = marketService.addOffer(offerMapper.offerCreateDtoToOffer(offer));
        return offerMapper.offerToOfferDto(created);
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