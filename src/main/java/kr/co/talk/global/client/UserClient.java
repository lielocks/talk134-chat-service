package kr.co.talk.global.client;

import java.util.List;

import kr.co.talk.domain.chatroom.dto.RequestDto;
import kr.co.talk.domain.chatroom.dto.RequestDto.ChatRoomEnterResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import kr.co.talk.domain.chatroom.dto.RequestDto.CreateChatroomResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto.TeamCodeResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto.UserIdResponseDto;
import kr.co.talk.global.config.FeignLoggingConfig;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "USER-SERVICE", configuration = FeignLoggingConfig.class)
public interface UserClient {
    /**
     * user-service에서 name또는 nickname으로 userId get
     * 
     * @param searchName
     * @return
     */
    @GetMapping("/user/id/{teamCode}/{searchName}")
    List<UserIdResponseDto> getUserIdByName(@PathVariable("teamCode") String teamCode,
            @PathVariable("searchName") String searchName);

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

    @GetMapping(value = "/user/enter-info/{userList}")
    List<ChatRoomEnterResponseDto> requiredEnterInfo(@RequestHeader("userId") long userId, @PathVariable List<Long> userList);

    @GetMapping(value = "/user/img-code")
    String getUserImgCode(@RequestHeader("userId") long userId);

}
