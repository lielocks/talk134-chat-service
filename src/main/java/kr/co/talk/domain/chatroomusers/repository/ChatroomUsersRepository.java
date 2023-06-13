package kr.co.talk.domain.chatroomusers.repository;

import kr.co.talk.domain.chatroom.model.Chatroom;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import kr.co.talk.domain.chatroomusers.entity.ChatroomUsers;

import java.util.List;

public interface ChatroomUsersRepository extends CrudRepository<ChatroomUsers, Long> {
    List<ChatroomUsers> findChatroomUsersByChatroom(Chatroom chatroom);
    List<ChatroomUsers> findChatroomUsersByUserId(Long userId);
    @Query("SELECT cu FROM ChatroomUsers cu WHERE cu.chatroom.chatroomId = :chatroomId AND cu.userId = :userId")
    ChatroomUsers findChatroomUsersByChatroomIdAndUserId(Long chatroomId, Long userId);
}
