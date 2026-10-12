package com.example.demo.limiting;

import lombok.Synchronized;
import lombok.Getter;
import lombok.Setter;
import java.util.function.LongSupplier;
import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketLimiterv2 implements RateLimitStrategy{
    ConcurrentHashMap<String, UserState> state = new ConcurrentHashMap<>();

    double maxTokens;
    double refillRate; // tokens per millisecond

    TokenBucketLimiterv2(RateLimitPolicy policy, LongSupplier clock){
        maxTokens = (double)policy.capacity();
        refillRate = (double)policy.capacity() / (double)policy.period().toMillis();
        state.put("default", new UserState("default", maxTokens, System.currentTimeMillis()));
    }

    public LimitDecision tryAcquire(){
        String user = "default";
        var object = state.computeIfAbsent(user, k -> new UserState(user, maxTokens, System.currentTimeMillis()));
        synchronized(object) {
            return tryAcquirePerUser(user);
        }
    }

    private LimitDecision tryAcquirePerUser(String user){
        var userState = state.get(user);
        refillTokens(userState);

        System.out.println("Current tokens for user: " + user + " = " + userState.getTokens());
        if(userState.getTokens() < 1){
            return LimitDecision.deny( (long)((1-userState.getTokens())/refillRate)*1000 );
        }

        try{
            Thread.sleep(10); // Simulate some processing time
        } catch (InterruptedException e) {
            return LimitDecision.deny((long)((1-userState.getTokens())/refillRate)*1000);
        }

        userState.setTokens(userState.getTokens() - 1);
        return LimitDecision.permit();
    }

    private void refillTokens(UserState userState){
        var now = System.currentTimeMillis();
        double tokensToAdd = (now - userState.getLastRefillTime()) * refillRate;
        System.out.println("Refilling tokens for user: " + userState.getUser() + ", tokens to add: " + tokensToAdd + ", interval: " + (now - userState.getLastRefillTime()) + ", refillRate: " + refillRate);
        userState.setTokens(Math.min(maxTokens, userState.getTokens() + tokensToAdd));
        userState.setLastRefillTime(now);
    }

    @Getter
    @Setter
    private class UserState {
        String user;
        double tokens;
        long lastRefillTime;

        UserState(String user, double tokens, long lastRefillTime) {
            this.user = user;
            this.tokens = tokens;
            this.lastRefillTime = lastRefillTime;
        }
    }
}
