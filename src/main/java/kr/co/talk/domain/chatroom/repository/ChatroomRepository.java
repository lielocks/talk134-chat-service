package kr.co.talk.domain.chatroom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import kr.co.talk.domain.chatroom.model.Chatroom;

public interface ChatroomRepository
        extends JpaRepository<Chatroom, Long>, ChatroomCustomRepository {
}
