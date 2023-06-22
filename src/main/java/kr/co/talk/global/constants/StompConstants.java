package kr.co.talk.global.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StompConstants {
    private static final String CHAT_ROOM_EMOTICON_DESTINATION = "/sub/chat/room/emoticon/";
    private static final String CHAT_ROOM_ENTER_DESTINATION = "/sub/chat/room/";
    private static final String CHAT_ROOM_PRIVATE_DESTINATION = "/sub/private/channel/";
    private static final String SUB_URL = "/sub/chat/room/question-notice/";
    private static final String TIMEOUT_NOTICE_SUB_URL = "/sub/chat/room/timeout/";
    private static final String CHAT_ROOM_SELECT_KEYWORD = "/sub/chat/keyword/";
    private static final String REGISTER_QUESTION_ORDER = "/sub/chat/question-order/";
    
    public static String getRoomEmoticonDestination(long roomId) {
        return CHAT_ROOM_EMOTICON_DESTINATION + roomId;
    }

    public static String getRoomEnterDestination(long roomId) {
        return CHAT_ROOM_ENTER_DESTINATION + roomId;
    }

    public static String getRoomUserDestination(long roomId, long userId) {
        return getRoomEmoticonDestination(roomId) + "/" + userId;
    }
    
    public static String generateQuestionNoticeSubUrl(long roomId) {
        return SUB_URL + roomId;
    }
    
    public static String getPrivateChannelDestination(long userId) {
        return CHAT_ROOM_PRIVATE_DESTINATION + userId;
    }

    public String generateTimeoutSubUrl(long roomId) {
        return TIMEOUT_NOTICE_SUB_URL + roomId;
    }

    public static String getChatUserSelectKeyword(long roomId, long userId) {
        return CHAT_ROOM_SELECT_KEYWORD + roomId + "/" + userId;
    }

    public static String getRegisterQuestionOrder(long roomId) {
        return REGISTER_QUESTION_ORDER + roomId;
    }

}
