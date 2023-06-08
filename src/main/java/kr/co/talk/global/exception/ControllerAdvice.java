package kr.co.talk.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ControllerAdvice {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> customException(CustomException e) {
        return new ResponseEntity<>(ErrorDto.createErrorDto(e.getCustomError()),
                HttpStatus.valueOf(e.getCustomError().getStatusCode()));
    }
    
    
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<?> feignException(FeignException e) {
        return new ResponseEntity<>(ErrorDto.createErrorDto(CustomError.FEIGN_ERROR),
                HttpStatus.valueOf(CustomError.FEIGN_ERROR.getStatusCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> commonException(Exception e) {
        log.error("", e);
        CustomError serverError = CustomError.SERVER_ERROR;
        return new ResponseEntity<>(ErrorDto.createErrorDto(serverError),
                HttpStatus.valueOf(serverError.getStatusCode()));
    }
}
