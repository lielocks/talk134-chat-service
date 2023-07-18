package kr.co.talk.domain.keyword.service;

import kr.co.talk.domain.chatroom.dto.ChatEnterDto;
import kr.co.talk.domain.chatroom.dto.RequestDto;
import kr.co.talk.domain.chatroom.dto.SocketFlagResponseDto;
import kr.co.talk.domain.chatroom.service.ChatRoomService;
import kr.co.talk.domain.chatroom.service.ChatService;
import kr.co.talk.domain.chatroomusers.dto.AllRegisteredDto;
import kr.co.talk.domain.chatroomusers.dto.KeywordSendDto;
import kr.co.talk.domain.chatroomusers.dto.KeywordSetDto;
import kr.co.talk.domain.chatroomusers.dto.QuestionCodeDto;
import kr.co.talk.domain.chatroomusers.entity.Keyword;
import kr.co.talk.domain.chatroomusers.entity.Question;
import kr.co.talk.domain.chatroomusers.repository.KeywordRepository;
import kr.co.talk.domain.chatroomusers.repository.QuestionRepository;
import kr.co.talk.domain.chatroomusers.service.KeywordService;
import kr.co.talk.global.client.UserClient;

import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import kr.co.talk.global.service.redis.RedisService;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@Slf4j
@ActiveProfiles(profiles = "test")
@ExtendWith(MockitoExtension.class)
public class KeywordServiceTest {

    @Autowired
    private KeywordService keywordService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ChatRoomService chatRoomService;

    @MockBean
    UserClient userClient;

    @BeforeEach
    void createChatRoom() {
        String teamCode = "abcdef";
        String chatroomName = "이솜, 이담, 해솔";

        long createUserId = 48L;
        List<Long> userList = List.of(48L, 53L, 62L);

        RequestDto.FindChatroomResponseDto mockChatroomResponseDto = new RequestDto.FindChatroomResponseDto();
        mockChatroomResponseDto.setTeamCode(teamCode);
        mockChatroomResponseDto.setUserRole("ROLE_USER");

        RequestDto.CreateChatroomResponseDto mockCreateChatroomResponseDto = new RequestDto.CreateChatroomResponseDto();
        mockCreateChatroomResponseDto.setTimeout(10);
        mockCreateChatroomResponseDto.setTeamCode(teamCode);
        mockCreateChatroomResponseDto.setChatroomName(chatroomName);

        doReturn(mockChatroomResponseDto).when(userClient).findChatroomInfo(anyLong());
        doReturn(mockCreateChatroomResponseDto).when(userClient)
                .requiredCreateChatroomInfo(anyLong(), anyList());

        chatRoomService.createChatroom(createUserId, userList);
    }


    @Test
    @DisplayName("24시간 내로 채팅방 참여 기록 있을 시 nickname map 의 1st recommendation 과 상관 없이 random question list 로 set")
    void withinOneDayRandomQuestion() {
        Keyword life = Keyword.builder().keywordId(1L).name("일상").build();
        Keyword relationship = Keyword.builder().keywordId(2L).name("관계").build();
        Keyword myself = Keyword.builder().keywordId(3L).name("나").build();
        keywordRepository.saveAll(Arrays.asList(life, relationship, myself));

        // statusMap 0 -> statusMap 상관없이 모든 keyword 에 해당 하는 question
        questionRepository.saveAll(
                Arrays.asList(
        Question.builder().questionId(1L).keyword(life).content("1-1").statusMap(1).guide("11").build(),
        Question.builder().questionId(2L).keyword(life).content("1-2").statusMap(2).guide("12").build(),
        Question.builder().questionId(3L).keyword(life).content("1-3").statusMap(3).guide("13").build(),
        Question.builder().questionId(4L).keyword(life).content("1-4").statusMap(0).guide("10").build(),
        Question.builder().questionId(5L).keyword(life).content("1-5").statusMap(1).guide("14").build(),
        Question.builder().questionId(6L).keyword(life).content("1-6").statusMap(2).guide("15").build(),
        Question.builder().questionId(7L).keyword(life).content("1-7").statusMap(3).guide("16").build(),
        Question.builder().questionId(8L).keyword(life).content("1-8").statusMap(0).guide("100").build(),

        Question.builder().questionId(9L).keyword(relationship).content("2-1").statusMap(1).guide("21").build(),
        Question.builder().questionId(10L).keyword(relationship).content("2-2").statusMap(2).guide("22").build(),
        Question.builder().questionId(11L).keyword(relationship).content("2-3").statusMap(3).guide("23").build(),
        Question.builder().questionId(12L).keyword(relationship).content("2-4").statusMap(0).guide("20").build(),
        Question.builder().questionId(13L).keyword(relationship).content("2-5").statusMap(1).guide("24").build(),
        Question.builder().questionId(14L).keyword(relationship).content("2-6").statusMap(2).guide("25").build(),
        Question.builder().questionId(15L).keyword(relationship).content("2-7").statusMap(3).guide("26").build(),
        Question.builder().questionId(16L).keyword(relationship).content("2-8").statusMap(0).guide("200").build(),

        Question.builder().questionId(17L).keyword(myself).content("3-1").statusMap(1).guide("31").build(),
        Question.builder().questionId(18L).keyword(myself).content("3-2").statusMap(2).guide("32").build(),
        Question.builder().questionId(19L).keyword(myself).content("3-3").statusMap(3).guide("33").build(),
        Question.builder().questionId(20L).keyword(myself).content("3-4").statusMap(0).guide("30").build(),
        Question.builder().questionId(21L).keyword(myself).content("3-5").statusMap(1).guide("34").build(),
        Question.builder().questionId(22L).keyword(myself).content("3-6").statusMap(2).guide("35").build(),
        Question.builder().questionId(23L).keyword(myself).content("3-7").statusMap(3).guide("36").build(),
        Question.builder().questionId(24L).keyword(myself).content("3-8").statusMap(0).guide("300").build()
        ));

        KeywordSendDto keywordSendDto = KeywordSendDto.builder().keywordCode(List.of(1L, 2L, 3L)).roomId(1L).userId(48L).build();
        doReturn("co-a-ng").when(userClient).getUserImgCode(48L); // ng equals question status map 1
        doReturn("co-d-sp").when(userClient).getUserImgCode(53L); // sp equals question status map 2
        doReturn("fl-d-ha").when(userClient).getUserImgCode(62L); // ha equals question status map 3
        List<Long> user53Keyword1QuestionList = List.of(1L, 4L, 5L, 8L);
        List<Long> user53Keyword2QuestionList = List.of(9L, 12L, 13L, 16L);
        List<Long> user53Keyword3QuestionList = List.of(17L, 20L, 21L, 24L);

        SocketFlagResponseDto questionWithFlag = keywordService.setQuestionWithFlag(keywordSendDto);
        assertTrue(user53Keyword1QuestionList.contains(questionWithFlag.getTopicList().get(0).getQuestionId()));
        assertTrue(user53Keyword2QuestionList.contains(questionWithFlag.getTopicList().get(1).getQuestionId()));
        assertTrue(user53Keyword3QuestionList.contains(questionWithFlag.getTopicList().get(2).getQuestionId()));

        List<RequestDto.ChatRoomEnterResponseDto> mockChatroomEnterResponseDto = new ArrayList<>();
        RequestDto.ChatRoomEnterResponseDto enterResponse1 =
                RequestDto.ChatRoomEnterResponseDto.builder().nickname("차가운 매의 낮잠").userName("이솜").userId(48L).profileUrl("https://134-back.s3.ap-northeast-2.amazonaws.com/profile/co-a-sp.png").build();
        mockChatroomEnterResponseDto.add(enterResponse1);

        RequestDto.ChatRoomEnterResponseDto enterResponse2 =
                RequestDto.ChatRoomEnterResponseDto.builder().nickname("차가운 바람의 일격").userName("이담").userId(53L).profileUrl("https://134-back.s3.ap-northeast-2.amazonaws.com/profile/co-d-bl.png").build();
        mockChatroomEnterResponseDto.add(enterResponse2);

        RequestDto.ChatRoomEnterResponseDto enterResponse3 =
                RequestDto.ChatRoomEnterResponseDto.builder().nickname("떠오르는 바람의 일격").userName("해솔").userId(62L).profileUrl("https://134-back.s3.ap-northeast-2.amazonaws.com/profile/fl-d-bl.png").build();
        mockChatroomEnterResponseDto.add(enterResponse3);

        doReturn(mockChatroomEnterResponseDto).when(userClient)
                .requiredEnterInfo(anyLong(), anyList());

        // roomId 1L 로 redis room key 먼저 set 하여 이미 참여한 채팅방 기록 생성
        ChatEnterDto chatEnterDto = ChatEnterDto.builder().selected(true).socketFlag(0).userId(48L).roomId(1L).build();
        chatService.sendChatMessage(chatEnterDto);

        SocketFlagResponseDto existingRoomResponse = keywordService.setQuestionWithFlag(keywordSendDto);
        List<Long> entered53Keyword1QuestionList = questionRepository.findByKeyword_KeywordId(1L).stream().map(Question::getQuestionId).collect(Collectors.toList());
        List<Long> entered53Keyword2QuestionList = questionRepository.findByKeyword_KeywordId(2L).stream().map(Question::getQuestionId).collect(Collectors.toList());
        List<Long> entered53Keyword3QuestionList = questionRepository.findByKeyword_KeywordId(3L).stream().map(Question::getQuestionId).collect(Collectors.toList());

        assertTrue(entered53Keyword1QuestionList.contains(existingRoomResponse.getTopicList().get(0).getQuestionId()));
        assertTrue(entered53Keyword2QuestionList.contains(existingRoomResponse.getTopicList().get(1).getQuestionId()));
        assertTrue(entered53Keyword3QuestionList.contains(existingRoomResponse.getTopicList().get(2).getQuestionId()));

    }

    @Test
    @DisplayName("질문 순서 등록은 1회 가능합니다")
    void setQuestionOrderTillTwice() {
        List<Long> keywordCode = new ArrayList<>(List.of(1L, 2L, 3L));
        List<Long> questionCode = new ArrayList<>(List.of(1L, 9L, 17L));
        redisService.pushQuestionList(1L, 53L, KeywordSetDto.builder().roomId(1L).registeredQuestionOrder(0).keywordCode(keywordCode).questionCode(questionCode).build());

        Collections.shuffle(questionCode);
        QuestionCodeDto questionCodeDto = QuestionCodeDto.builder().questionCodeList(questionCode).userId(53L).roomId(1L).build();
        keywordService.setQuestionOrder(questionCodeDto);

        CustomException customException = assertThrows(CustomException.class, () -> {
            keywordService.setQuestionOrder(questionCodeDto);
        });

        assertEquals(customException.getCustomError(), CustomError.QUESTION_ORDER_CHANCE_ONCE);
    }
}
