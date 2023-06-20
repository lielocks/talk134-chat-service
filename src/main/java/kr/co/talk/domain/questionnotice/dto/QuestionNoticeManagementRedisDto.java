package kr.co.talk.domain.questionnotice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import kr.co.talk.domain.chatroom.dto.RequestDto;

/**
 * 질문 알림 조회 시 대화방 별 스피커의 순서와 현재 스피커를 관리하기 위한 dto
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionNoticeManagementRedisDto {
    /**
     * 1인당 질문 수
     */
    public static final int QUESTION_NUMBERS = 3;
    
    /**
     * 현재 스피커 index. 0부터 시작
     */
    private int currentSpeakerIndex;

    /**
     * 현재 스피커의 질문 index. 0부터 시작
     */
    private int currentQuestionIndex;

    /**
     * 질문 스피커 순서 랜덤으로 배열한 것
     */
    private List<RequestDto.ChatRoomEnterResponseDto> speakerQueue;
    
    public boolean isFinalQuestion() {
        return currentQuestionIndex + 1 == QUESTION_NUMBERS;
    }
    
    public boolean isFinalSpeaker() {
        return currentSpeakerIndex + 1 == speakerQueue.size();
    }
    
    public void incrementCurrentSpeakerIndex() {
        this.currentSpeakerIndex++;
    }
    
    public void incrementCurrentQuestionIndex() {
        this.currentQuestionIndex++;
    }
    
    public void resetQuestionIndex() {
        this.currentQuestionIndex = 0;
    }
}
