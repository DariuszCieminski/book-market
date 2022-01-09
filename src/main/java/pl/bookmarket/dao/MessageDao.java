package pl.bookmarket.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.bookmarket.model.Message;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageDao extends CrudRepository<Message, Long> {

    @EntityGraph(attributePaths = {"sender", "receiver"})
    List<Message> findMessagesByReceiverId(Long id);

    @EntityGraph(attributePaths = {"sender", "receiver"})
    List<Message> findMessagesByReadFalseAndReceiverId(Long id);

    @Query("from Message m join m.receiver r join m.sender s where r.id=?1 or s.id=?1")
    List<Message> findAllMessagesForUser(Long id);

    @Override
    @EntityGraph(attributePaths = {"sender", "receiver"})
    Optional<Message> findById(Long aLong);
}