package kr.co.talk.domain.chatroom.dto;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 채팅방 끝나기 5분전에 알림주고, 채팅방 종료를 시켜주기위한 dto
 */
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatroomNoticeDto implements Serializable{
    private static final long serialVersionUID = 1L;
    
    private long roomId;
    private long timeout;
    private long createTime;
    private boolean isNotice; // 5분전 안내 
    private boolean active; // 모든 팀원이 준비가 되었을 때 부터 시작
}
