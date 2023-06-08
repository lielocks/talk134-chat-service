package kr.co.talk.global.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import kr.co.talk.global.exception.CustomError;
import kr.co.talk.global.exception.CustomException;
import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class FeignAop {
    @Around("execution(* kr.co.talk.global.client.*.*(..))")
    public Object loggingAndThrowException(
            ProceedingJoinPoint pjp) throws Throwable {
        Object result = "";
        try {
            log.info("client call before");
            result = pjp.proceed();
            log.info("client call after");
        } catch (Exception e) {
            log.error("USER Client Error", e);
            throw new CustomException(CustomError.USER_DOES_NOT_EXIST);
        }
        return result;
    }

}
