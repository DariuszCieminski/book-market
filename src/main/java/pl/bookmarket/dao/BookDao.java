package pl.bookmarket.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.bookmarket.model.Book;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookDao extends CrudRepository<Book, Long> {

    @EntityGraph(attributePaths = {"genre", "owner"})
    List<Book> getBooksByOwnerId(Long id);

    //get books marked for sale not owned by current user and for which the current user hasn't made an offer yet
    @Query("from Book book join book.owner owner join book.genre where book.forSale=true and owner.id<>?1 and book.id not in "
            + "(select b.id from Book b join b.offers o join o.buyer buyer where buyer.id=?1)")
    List<Book> getBooksForSale(Long currentUserId);

    @Override
    @EntityGraph(attributePaths = {"genre", "owner"})
    Optional<Book> findById(Long aLong);
}