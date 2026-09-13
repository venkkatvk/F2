package com.flashsale.order.resilience;

public class ServiceDegradedException extends RuntimeException {

    public ServiceDegradedException(String message) {
        super(message);
    }
}
