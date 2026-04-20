package com.nicasia.cyberintel.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, int[]> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> windowStart = new ConcurrentHashMap<>();

    // FIX Bug 2: Raised to 500 per minute — admin panel polls multiple
    // endpoints continuously which was exhausting the old limit.
    private static final int MAX_REQUESTS = 500;
    private static final int WINDOW_MINUTES = 1;

    public boolean isAllowed(String ipAddress) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowBegin = windowStart.getOrDefault(ipAddress, now);

        if (now.isAfter(windowBegin.plusMinutes(WINDOW_MINUTES))) {
            requestCounts.put(ipAddress, new int[]{1});
            windowStart.put(ipAddress, now);
            return true;
        }

        int[] count = requestCounts.getOrDefault(ipAddress, new int[]{0});
        count[0]++;
        requestCounts.put(ipAddress, count);

        return count[0] <= MAX_REQUESTS;
    }

    public int getRemainingRequests(String ipAddress) {
        int[] count = requestCounts.getOrDefault(ipAddress, new int[]{0});
        return Math.max(0, MAX_REQUESTS - count[0]);
    }
}
