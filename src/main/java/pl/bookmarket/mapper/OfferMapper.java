package pl.bookmarket.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.bookmarket.dto.OfferCreateDto;
import pl.bookmarket.dto.OfferDto;
import pl.bookmarket.model.Offer;

@Mapper(uses = {BookMapper.class, UserMapper.class})
public interface OfferMapper {

    OfferDto offerToOfferDto(Offer offer);

    @Mapping(target = "book.id", source = "bookId")
    Offer offerCreateDtoToOffer(OfferCreateDto offerCreateDto);
}