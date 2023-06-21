package kr.co.talk.global.constants;

public class StompConstants {
    private static final String CHAT_ROOM_EMOTICON_DESTINATION = "/sub/chat/room/emoticon";
    private static final String CHAT_ROOM_ENTER_DESTINATION = "/sub/chat/room";
    private static final String CHAT_ROOM_PRIVATE_DESTINATION = "/sub/private/channel";
    private static final String SUB_URL = "/sub/chat/room/question-notice/";
    
    public static String getRoomDestination(long roomId) {
        return String.format("%s/%s", CHAT_ROOM_EMOTICON_DESTINATION, roomId);
    }

    public static String getRoomUserDestination(long roomId, long userId) {
        return String.format("%s/%s", getRoomDestination(roomId), userId);
    }
    
    public static String generateSubUrl(long roomId) {
        return SUB_URL + roomId;
    }
    
    public static String getPrivateChannelDestination(long userId) {
        return String.format("%s/%s", CHAT_ROOM_PRIVATE_DESTINATION, userId);
    }

}
