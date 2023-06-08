package kr.co.talk.global.exception;

import org.springframework.http.HttpStatus;

public enum CustomError {
    // 채팅방
    USER_NUMBER_ERROR(2000, "대화방 참가자는 1명 이상이어야 합니다.", HttpStatus.BAD_REQUEST.value()),

    // 회원
    USER_DOES_NOT_EXIST(2001, "USER SERVICE API ERROR", HttpStatus.BAD_REQUEST.value()),
    CHATROOM_DOES_NOT_EXIST(2002, "해당 채팅방이 존재하지 않습니다.", HttpStatus.NO_CONTENT.value()),
    

    // 공통
    SERVER_ERROR(3000, "알수 없는 문제가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value()),
    FEIGN_ERROR(3001, "다른 API 호출에 실패하였습니다.", HttpStatus.BAD_GATEWAY.value());

    private int errorCode;
    private String message;
    private int statusCode;

    public int getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }


    CustomError(int errorCode, String message, int statusCode) {
        this.errorCode = errorCode;
        this.message = message;
        this.statusCode = statusCode;
    }
}
