package com.library.exception;

/**
 * 乐观锁冲突异常
 * 用于并发操作冲突时抛出
 */
public class OptimisticLockException extends RuntimeException {

    public OptimisticLockException(String message) {
        super(message);
    }

    public OptimisticLockException(String message, Throwable cause) {
        super(message, cause);
    }
}
