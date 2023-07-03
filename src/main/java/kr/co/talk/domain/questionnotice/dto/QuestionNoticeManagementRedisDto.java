package kr.co.talk.domain.questionnotice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 질문 알림 조회 시 대화방 별 스피커의 순서와 현재 스피커를 관리하기 위한 dto
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionNoticeManagementRedisDto {
    /**
     * 질문 스피커 순서 랜덤으로 배열한 것
     */
    private List<QuestionUserMap> questionList;

    private List<QuestionNoticeResponseDto.User> userList;

    @Builder
    @Data
    public static class QuestionUserMap {
        private long userId;
        private long questionCode;
    }

    public boolean isFinalQuestion(int questionNumber) {
        return questionNumber == questionList.size();
    }
}
