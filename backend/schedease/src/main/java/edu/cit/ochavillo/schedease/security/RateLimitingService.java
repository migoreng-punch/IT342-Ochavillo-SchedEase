package edu.cit.ochavillo.schedease.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    // This map remembers the bucket for each IP address
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String ipAddress, RateLimitPlan plan) {
        String cacheKey = ipAddress + "-" + plan.name();

        return cache.computeIfAbsent(cacheKey, key -> newBucket(plan));
    }

    private Bucket newBucket(RateLimitPlan plan) {
        Refill refill = Refill.intervally(plan.getLimit(), plan.getDuration());
        Bandwidth bandwidthLimit = Bandwidth.classic(plan.getLimit(), refill);

        return Bucket.builder()
                .addLimit(bandwidthLimit)
                .build();
    }
}