package kr.co.talk.global.exception;

import org.springframework.http.HttpStatus;

public enum CustomError {
    // 채팅방
    USER_NUMBER_ERROR(2000, "대화방 참가자는 1명 이상이어야 합니다.", HttpStatus.BAD_REQUEST.value()),
    TEAM_CODE_ERROR(2001, "대화방 참가자는 같은 팀이어야만 합니다.", HttpStatus.BAD_REQUEST.value()),
    CHATROOM_DOES_NOT_EXIST(2002, "해당 채팅방이 존재하지 않습니다.", HttpStatus.NOT_FOUND.value()),

    // 회원
    USER_DOES_NOT_EXIST(1035, "해당 사용자가 존재하지 않습니다.", HttpStatus.NOT_FOUND.value()),

    // 키워드, 질문
    KEYWORD_DOES_NOT_MATCH(4001, "해당 키워드에 해당하는 질문이 존재하지 않습니다.", HttpStatus.BAD_REQUEST.value()),
    KEYWORD_DOES_NOT_EXIST(4002, "해당 키워드가 존재하지 않습니다.", HttpStatus.BAD_REQUEST.value()),
    QUESTION_LIST_SIZE_MISMATCH(4003, "해당 리스트의 질문 갯수가 3이 아닙니다.", HttpStatus.BAD_REQUEST.value()),
    CHATROOM_USER_ALREADY_JOINED(4004, "이미 참가하신 채팅방이 존재합니다.", HttpStatus.BAD_REQUEST.value()),
    QUESTION_ALREADY_REGISTERED(4005, "이미 질문을 등록하였습니다.", HttpStatus.BAD_REQUEST.value()),

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
