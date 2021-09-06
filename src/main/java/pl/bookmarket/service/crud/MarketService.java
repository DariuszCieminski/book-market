package pl.bookmarket.service.crud;

import pl.bookmarket.model.Offer;

import java.util.List;

public interface MarketService {
    List<Offer> getOffersForBook(Long bookId);

    List<Offer> getOffersByUserLogin(String login);

    Offer addOffer(Offer offer);

    void acceptOffer(Long id);

    void deleteOffer(Long id);
}