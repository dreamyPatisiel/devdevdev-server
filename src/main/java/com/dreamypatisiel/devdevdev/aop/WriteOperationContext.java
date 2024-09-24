package com.dreamypatisiel.devdevdev.aop;

public class WriteOperationContext {

    // ThreadLocal을 사용하여 트랜잭션별로 쓰기 작업 감지 상태 관리
    private static final ThreadLocal<Boolean> writeOperationDetected = ThreadLocal.withInitial(() -> false);

    // 쓰기 작업이 감지되면 true로 설정
    public static void setWriteOperationDetected(boolean detected) {
        writeOperationDetected.set(detected);
    }

    // 쓰기 작업 감지 상태 조회
    public static boolean isWriteOperationDetected() {
        return writeOperationDetected.get();
    }

    // 트랜잭션이 끝날 때 ThreadLocal을 해제
    public static void clear() {
        writeOperationDetected.remove();
    }
}
