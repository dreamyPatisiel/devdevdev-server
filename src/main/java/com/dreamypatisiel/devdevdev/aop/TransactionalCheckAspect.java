package com.dreamypatisiel.devdevdev.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@Profile("test")
public class TransactionalCheckAspect {

    // 서비스 메서드 실행 전 WriteOperationContext 초기화
    @Before("execution(public * com.dreamypatisiel.devdevdev.domain.service..*(..))")
    public void checkWriteOperationsInService() {
        // 트랜잭션 시작 시 ThreadLocal 초기화
        WriteOperationContext.clear();
    }

    @AfterReturning("execution(public * com.dreamypatisiel.devdevdev.domain.service..*(..)) || @annotation(org.springframework.transaction.annotation.Transactional)")
    public void checkUpdateAndTransactional(JoinPoint joinPoint) throws Throwable {
        // 쓰기 작업이 감지되었는지 확인
        if (!WriteOperationContext.isWriteOperationDetected()) {
            return;
        }

        // 트랜잭션 적용 여부 확인 및 예외 던지기
        checkTransactional(joinPoint);
    }

    private void checkTransactional(JoinPoint joinPoint) throws Exception {
        // 쓰기 작업이 감지되었을 경우
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();

        // 클래스와 메서드에 @Transactional 어노테이션이 있는지 확인
        Transactional classTransactional = targetClass.getAnnotation(Transactional.class);
        Transactional methodTransactional = method.getAnnotation(Transactional.class);

        Transactional transactional = null;
        if (methodTransactional != null) {
            transactional = methodTransactional;
        } else if (classTransactional != null) {
            transactional = classTransactional;
        }

        if(transactional == null || transactional.readOnly()) {
            throw new RuntimeException("트랜잭션 적용이 필요합니다.");
        }
    }
}