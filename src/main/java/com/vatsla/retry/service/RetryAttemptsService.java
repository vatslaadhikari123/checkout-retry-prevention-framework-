package com.vatsla.retry.service;

import com.google.inject.Singleton;
import com.vatsla.retry.model.BaseDTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class RetryAttemptsService {

    // Key: "jobType:entityId" -> Value: attemptCount
    private final Map<String, Integer> attemptStore = new ConcurrentHashMap<>();

    private String buildKey(String jobType, String entityId) {
        return jobType + ":" + entityId;
    }

    /**
     * Persists or increments the retry count for a business object.
     */
    public synchronized void recordAttempt(String requestId, String jobType, BaseDTO resource) {
        String key = buildKey(jobType, resource.getId());
        attemptStore.merge(key, 1, Integer::sum);
    }

    /**
     * Retrieves all entity IDs that have reached or exceeded the maximum retry limit.
     */
    public List<String> findIdsExceedingMaxRetries(String requestId, String jobType, int maxLimit) {
        String prefix = jobType + ":";
        return attemptStore.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .filter(entry -> entry.getValue() >= maxLimit)
                .map(entry -> entry.getKey().substring(prefix.length()))
                .collect(Collectors.toList());
    }

    /**
     * Helper for tests to inspect current attempt count.
     */
    public int getAttempts(String jobType, String entityId) {
        return attemptStore.getOrDefault(buildKey(jobType, entityId), 0);
    }

    public void clear() {
        attemptStore.clear();
    }
}
