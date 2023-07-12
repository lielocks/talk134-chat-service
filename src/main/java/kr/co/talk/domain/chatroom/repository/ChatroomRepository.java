package kr.co.talk.domain.chatroom.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import kr.co.talk.domain.chatroom.model.Chatroom;

public interface ChatroomRepository
        extends JpaRepository<Chatroom, Long>, ChatroomCustomRepository {
    Chatroom findChatroomByChatroomId(Long id);

    @Query("select c from Chatroom c join fetch c.chatroomUsers where c.chatroomId=:id")
    Optional<Chatroom> findChatroomByChatroomIdFetch(@Param("id") Long id);
}
