package pl.bookmarket.testhelpers.datafactory;

import pl.bookmarket.dto.OfferCreateDto;

public class OfferCreateBuilder {

    private final OfferCreateDto offer = new OfferCreateDto();

    public OfferCreateDto build() {
        return offer;
    }

    public OfferCreateBuilder withComment(String comment) {
        offer.setComment(comment);
        return this;
    }

    public OfferCreateBuilder withBookId(Long bookId) {
        offer.setBookId(bookId);
        return this;
    }
}