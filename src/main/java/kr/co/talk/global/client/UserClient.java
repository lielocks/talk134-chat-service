package kr.co.talk.global.client;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import kr.co.talk.domain.chatroom.dto.RequestDto.CreateChatroomResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto.TeamCodeResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto.UserIdResponseDto;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {
    /**
     * user-service에서 name또는 nickname으로 userId get
     * 
     * @param searchName
     * @return
     */
    @GetMapping("/user/id/{searchName}")
    List<UserIdResponseDto> getUserIdByName(@PathVariable("searchName") String searchName);

    /**
     * user-service로부터 teamCode get
     * 
     * @param userId
     * @return
     */
    @GetMapping(value = "/user/teamCode/{userId}")
    TeamCodeResponseDto getTeamCode(@PathVariable(value = "userId") long userId);

    /**
     * user-service로부터 timeout get
     * 
     * @param userId
     * @return
     */
    @PostMapping(value = "/user/required-create-chatroom-info/{userId}")
    CreateChatroomResponseDto requiredCreateChatroomInfo(
            @PathVariable(value = "userId") long userId, List<Long> userList);
}
