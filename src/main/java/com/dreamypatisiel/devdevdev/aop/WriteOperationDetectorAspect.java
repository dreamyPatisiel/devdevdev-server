package com.dreamypatisiel.devdevdev.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Profile("test")
public class WriteOperationDetectorAspect {
    // 실제 쓰기 작업이 발생할 때 감지
    @Around("execution(* org.springframework.data.repository.CrudRepository.save*(..)) || " +
            "execution(* org.springframework.data.repository.CrudRepository.delete*(..)) || " +
            "execution(* org.springframework.data.jpa.repository.JpaRepository.save*(..)) || " +
            "execution(* org.springframework.data.jpa.repository.JpaRepository.delete*(..)) || " +
            "execution(* com.dreamypatisiel.devdevdev.domain.repository..*.update*(..))")
    public Object trackWriteOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        // 쓰기 작업이 감지되면 상태를 true로 설정
        WriteOperationContext.setWriteOperationDetected(true);

        return joinPoint.proceed();
    }
}