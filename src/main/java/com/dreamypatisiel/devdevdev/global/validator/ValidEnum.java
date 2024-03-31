package com.dreamypatisiel.devdevdev.global.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 해당 annotation이 실행 할 ConstraintValidator 구현체를 `EnumValidator`로 지정
@Constraint(validatedBy = {EnumValidator.class})
// 해당 어노테이션은 메소드, 필드, 파라미터에 적용 가능
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
// 어노테이션 Runtime 까지 유지
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnum {
    String message() default "올바른 입력 값이 아닙니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    Class<? extends Enum<?>> enumClass();
}
