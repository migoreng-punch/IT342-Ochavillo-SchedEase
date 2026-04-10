package edu.cit.ochavillo.schedease.security;

import lombok.Getter;

import java.time.Duration;

@Getter
public enum RateLimitPlan {

    // 🛡️ TIER 1: Highly Sensitive (Logins, Registration)
    AUTH(5, Duration.ofMinutes(1)),

    // 🛡️ TIER 2: Expensive Queries (Searching Establishments)
    SEARCH(30, Duration.ofMinutes(1)),

    // 🛡️ TIER 3: Standard User Actions (Rescheduling, Profile Updates)
    STANDARD(100, Duration.ofMinutes(1));

    private final int limit;
    private final Duration duration;

    RateLimitPlan(int limit, Duration duration) {
        this.limit = limit;
        this.duration = duration;
    }
}
