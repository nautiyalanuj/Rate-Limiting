package com.example.demo.annotations;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)   // how long it's kept
@Target(ElementType.METHOD)           // where it can be applied
@Documented                           // appears in Javadoc
public @interface RateLimiter {
    String value();
}
