package pl.bookmarket.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import pl.bookmarket.model.Book;

public interface BookDao extends CrudRepository<Book, Long> {

    @EntityGraph(attributePaths = {"genre", "owner"})
    List<Book> getBooksByOwner_Login(String login);

    //get books marked for sale not owned by current user and for which the current user hasn't made an offer yet
    @EntityGraph(attributePaths = "genre")
    @Query("from Book book join book.owner owner where book.forSale=true and owner.login<>?1 and book.id not in "
           + "(select b.id from Book b join b.offers o join o.buyer buyer where buyer.login=?1)")
    List<Book> getBooksForSale(String login);

    @Override
    @EntityGraph(attributePaths = {"genre", "owner"})
    Optional<Book> findById(Long aLong);
}