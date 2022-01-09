package pl.bookmarket.service.crud;

import pl.bookmarket.model.Offer;

import java.util.List;
import java.util.Optional;

public interface MarketService {
    List<Offer> getOffersForBook(Long bookId);

    List<Offer> getOffersByUserId(Long id);

    Optional<Offer> getOfferById(Long id);

    Offer addOffer(Offer offer);

    void acceptOffer(Long id);

    void deleteOffer(Long id);
}