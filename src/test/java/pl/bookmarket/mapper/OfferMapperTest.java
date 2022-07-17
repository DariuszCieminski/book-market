package pl.bookmarket.mapper;

import org.junit.jupiter.api.Test;
import pl.bookmarket.dto.OfferCreateDto;
import pl.bookmarket.dto.OfferDto;
import pl.bookmarket.model.Offer;
import pl.bookmarket.testhelpers.datafactory.BookBuilder;
import pl.bookmarket.testhelpers.datafactory.OfferCreateBuilder;
import pl.bookmarket.testhelpers.datafactory.UserBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OfferMapperTest {

    private final OfferMapper offerMapper;

    public OfferMapperTest() {
        UserMapper userMapper = new UserMapperImpl(new RoleMapperImpl());
        BookMapper bookMapper = new BookMapperImpl(new GenreMapperImpl(), userMapper);
        offerMapper = new OfferMapperImpl(bookMapper, userMapper);
    }

    @Test
    void offerToOfferDto() {
        Offer offer = new Offer();
        offer.setId(666L);
        offer.setComment("Can you sell this book cheaper?");
        offer.setBook(BookBuilder.getDefaultBook().build());
        offer.setBuyer(UserBuilder.getAdminUser().build());

        OfferDto offerDto = offerMapper.offerToOfferDto(offer);

        assertEquals(offer.getId(), offerDto.getId());
        assertEquals(offer.getComment(), offerDto.getComment());
        assertEquals(offer.getBook().getId(), offerDto.getBook().getId());
        assertEquals(offer.getBook().getTitle(), offerDto.getBook().getTitle());
        assertEquals(offer.getBook().isForSale(), offerDto.getBook().isForSale());
        assertEquals(offer.getBook().getPrice(), offerDto.getBook().getPrice());
        assertEquals(offer.getBuyer().getId(), offerDto.getBuyer().getId());
        assertEquals(offer.getBuyer().getLogin(), offerDto.getBuyer().getLogin());
        assertEquals(offer.getBook().getOwner().getId(), offerDto.getBook().getOwner().getId());
        assertEquals(offer.getBook().getOwner().getLogin(), offerDto.getBook().getOwner().getLogin());
    }

    @Test
    void offerCreateDtoToOffer() {
        OfferCreateDto offerCreateDto = new OfferCreateBuilder()
                .withBookId(999L)
                .withComment("When can we finalize the transaction?")
                .build();

        Offer offer = offerMapper.offerCreateDtoToOffer(offerCreateDto);

        assertEquals(offerCreateDto.getBookId(), offer.getBook().getId());
        assertEquals(offerCreateDto.getComment(), offer.getComment());
    }
}