package com.authService.AOP;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SendNotification {
    String topic();
    String eventType(); // e.g., "USER_REGISTERED", "STATUS_CHANGED"
}