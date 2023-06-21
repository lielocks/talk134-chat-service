package kr.co.talk.global.client;

import java.util.List;

import kr.co.talk.domain.chatroom.dto.RequestDto.ChatRoomEnterResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import kr.co.talk.domain.chatroom.dto.RequestDto.CreateChatroomResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto.FindChatroomResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto.TeamCodeResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto.UserIdResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto.UserNameResponseDto;
import kr.co.talk.domain.chatroom.dto.RequestDto.UserStatusDto;
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
    @GetMapping(value = "/user/findChatroomInfo/{userId}")
    FindChatroomResponseDto findChatroomInfo(@PathVariable(value = "userId") long userId);

    /**
     * user-service로부터 timeout get
     * 
     * @param userId
     * @return
     */
    @PostMapping(value = "/user/required-create-chatroom-info/{userId}")
    CreateChatroomResponseDto requiredCreateChatroomInfo(
            @PathVariable(value = "userId") long userId, List<Long> userList);


    /**
     * user-service로부터 user의 status get
     * 
     * @param userId
     * @return
     */
    @GetMapping("/user/status/{userId}")
    public UserStatusDto getUserStaus(@PathVariable("userId") long userId);

    @GetMapping(value = "/user/enter-info/{userList}")
    List<ChatRoomEnterResponseDto> requiredEnterInfo(@RequestHeader("userId") long userId, @PathVariable("userList") List<Long> userList);
    
    @PutMapping("/user/changeStatus/{userId}")
    ResponseEntity<?> changeStatus(@PathVariable("userId") long userId, UserStatusDto updateRequestStatusDto);

    @GetMapping(value = "/user/img-code")
    String getUserImgCode(@RequestHeader("userId") long userId);

    
    @GetMapping("/user/findTeamCode/{userId}")
    public TeamCodeResponseDto findTeamCode(@PathVariable("userId") long userId);
    
    @GetMapping("/user/userName/nickname/{userIds}")
    public List<UserNameResponseDto> userNameNickname(@PathVariable("userIds") List<Long> userIds);
}
