package pl.bookmarket.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.bookmarket.model.Offer;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferDao extends CrudRepository<Offer, Long> {

    @EntityGraph(attributePaths = {"buyer", "book", "book.genre"})
    List<Offer> getOffersByBuyerId(Long id);

    @EntityGraph(attributePaths = {"buyer", "book"})
    List<Offer> getOffersByBookId(Long id);

    @Override
    @EntityGraph(attributePaths = {"buyer", "book", "book.owner", "book.offers"})
    Optional<Offer> findById(Long aLong);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Offer where book.id=?1")
    void deleteAllOffersForBook(Long id);
}