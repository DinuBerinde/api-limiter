package com.dinuberinde.api.limiter;

public class ApiLimiterException extends RuntimeException {

    public ApiLimiterException(String message) {
        super(message);
    }
}
