package com.example.demo.limiting;

import java.util.function.LongSupplier;

public class SlidingWindowLimiterv2 implements RateLimitStrategy{

    SlidingWindowLimiterv2(RateLimitPolicy policy, LongSupplier clock){
    }

    public LimitDecision tryAcquire(){
        System.out.println("SlidingWindowLimiterv2 tryAcquire called");
        return LimitDecision.permit();
    }
}
