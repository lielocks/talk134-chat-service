package kr.co.talk.domain.keyword.service;

import fixture.ChatFixture;
import fixture.KeywordFixture;
import kr.co.talk.domain.chatroom.dto.ChatEnterDto;
import kr.co.talk.domain.chatroom.dto.RequestDto;
import kr.co.talk.domain.chatroom.dto.SocketFlagResponseDto;
import kr.co.talk.domain.chatroom.service.ChatRoomService;
import kr.co.talk.domain.chatroom.service.ChatService;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private QuestionRepository questionRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ChatRoomService chatRoomService;

    @MockBean
    UserClient userClient;

    ChatFixture chatFixture;

    KeywordFixture keywordFixture;

    @BeforeEach
    void createChatRoom() {
        chatFixture = new ChatFixture();
        chatFixture.setUpBeforeTest(userClient, chatRoomService);
    }

    @Test
    @DisplayName("해당 test 는 user img code 마지막 두자릿수와 question status map 간의 상관관계를 나타냅니다")
    void imgCodeAndQuestionStatusMap() {
        // given
        doReturn("ng").when(userClient)
                .getUserImgCode(10L);
        doReturn("sp").when(userClient)
                .getUserImgCode(11L);
        doReturn("ha").when(userClient)
                .getUserImgCode(12L);
        doReturn("fu").when(userClient)
                .getUserImgCode(13L);
        doReturn("bl").when(userClient)
                .getUserImgCode(14L);
        // ng 1 sp 2 ha 3 fu 4 bl 5
        // when
        Integer userStatusMap10L = keywordService.setUserStatusMap(10L);
        Integer userStatusMap11L = keywordService.setUserStatusMap(11L);
        Integer userStatusMap12L = keywordService.setUserStatusMap(12L);
        Integer userStatusMap13L = keywordService.setUserStatusMap(13L);
        Integer userStatusMap14L = keywordService.setUserStatusMap(14L);

        // then
        assertEquals(userStatusMap10L, 1);
        assertEquals(userStatusMap11L, 2);
        assertEquals(userStatusMap12L, 3);
        assertEquals(userStatusMap13L, 4);
        assertEquals(userStatusMap14L, 5);
    }

    @Test
    @Transactional
    @DisplayName("Question Algorithm : 24시간 내로 채팅방 참여 기록 있을 시 nickname map 의 1st recommendation 과 상관 없이 random question list 로 set")
    void withinOneDayRandomQuestion() {
        // given
        keywordFixture = new KeywordFixture();
        List<Keyword> keywordMockData = keywordFixture.setUpKeywordMockData();
        keywordRepository.saveAll(keywordMockData);
        List<Question> questionMockData = keywordFixture.setUpQuestionMockData();
        questionRepository.saveAll(questionMockData);

        // when
        // statusMap : user 가 회원 가입할때 선택한 닉네임이 userImgCode 로 저장되어 ImgCode 끝자리 두자릿수별 부여되는 번호와 status Map 이 일치하는 question 할당
        // statusMap 0 -> statusMap 상관없이 모든 keyword 에 할당 가능한 question
        KeywordSendDto keywordSendDto = KeywordSendDto.builder().keywordCode(List.of(1L, 2L, 3L)).roomId(1L).userId(48L).build();
        doReturn("co-a-ng").when(userClient).getUserImgCode(48L); // userImgCode 끝자리 ng 는 question status map 1
        doReturn("co-d-sp").when(userClient).getUserImgCode(53L); // userImgCode 끝자리 sp 는 question status map 2
        doReturn("fl-d-ha").when(userClient).getUserImgCode(62L); // userImgCode 끝자리 ha 는 question status map 3
        List<Long> user53Keyword1QuestionList = List.of(1L, 6L, 18L, 25L);
        List<Long> user53Keyword2QuestionList = List.of(2L, 10L, 13L, 16L);
        List<Long> user53Keyword3QuestionList = List.of(5L, 9L, 15L, 21L);

        SocketFlagResponseDto questionWithFlag = keywordService.setQuestionWithFlag(keywordSendDto);
        // 채팅방 참여 기록이 없을 때는 status map 에 맞는 question 만 할당됨
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

        // roomId 1L 로 redis room key 먼저 set 하여 이미 참여한 채팅방이 있다는 기록 생성
        ChatEnterDto chatEnterDto = ChatEnterDto.builder().selected(true).socketFlag(0).userId(48L).roomId(1L).build();
        chatService.sendChatMessage(chatEnterDto);

        // then
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
        // given
        List<Long> keywordCode = new ArrayList<>(List.of(1L, 2L, 3L));
        List<Long> questionCode = new ArrayList<>(List.of(1L, 15L, 28L));
        redisService.pushQuestionList(1L, 53L, KeywordSetDto.builder().roomId(1L).registeredQuestionOrder(0).keywordCode(keywordCode).questionCode(questionCode).build());

        // when
        Collections.shuffle(questionCode);
        QuestionCodeDto questionCodeDto = QuestionCodeDto.builder().questionCodeList(questionCode).userId(53L).roomId(1L).build();
        // 첫번째 질문 순서 등록
        keywordService.setQuestionOrder(questionCodeDto);

        // then
        CustomException customException = assertThrows(CustomException.class, () -> {
            keywordService.setQuestionOrder(questionCodeDto);
        });

        assertEquals(customException.getCustomError(), CustomError.QUESTION_ORDER_CHANCE_ONCE);
    }
}
