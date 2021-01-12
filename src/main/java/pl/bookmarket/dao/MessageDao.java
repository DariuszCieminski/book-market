package pl.bookmarket.dao;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import pl.bookmarket.model.Message;

public interface MessageDao extends CrudRepository<Message, Long> {

    @EntityGraph(attributePaths = "sender")
    List<Message> findMessagesByReadFalseAndReceiver_Login(String login);

    @Query("from Message m join m.receiver r join m.sender s where r.login=?1 or s.login=?1")
    List<Message> findAllMessages(String login);

    @EntityGraph(attributePaths = "sender")
    List<Message> findMessagesByReceiver_Login(String login);
}