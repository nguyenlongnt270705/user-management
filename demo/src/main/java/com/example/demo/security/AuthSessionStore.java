package com.example.demo.security;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class AuthSessionStore {
    private static final String SESSION_PREFIX = "auth:session:";
    private static final String REFRESH_PREFIX = "auth:refresh:";
    private final RedissonClient redissonClient;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    // create session
    public String startSession(Long userId) {
        String sid = UUID.randomUUID().toString();
        saveSession(userId, sid);
        return sid;
    }

    public void saveSession(Long userId, String sid) {
/*        RBucket<String> bucket = redissonClient.getBucket(SESSION_PREFIX + userId);
        bucket.set(sid, Duration.ofMillis(refreshExpirationMs));*/
        bucket(SESSION_PREFIX + userId).set(sid, Duration.ofMillis(refreshExpirationMs));
    }


    // get current session
    public String getCurrentSid(Long userId) {
//        RBucket<String> bucket = redissonClient.getBucket(SESSION_PREFIX + userId);
//        return bucket.get();
        return bucket(SESSION_PREFIX + userId).get();
    }

    // delete session
    public void deleteSession(Long userId) {
//        RBucket<String> bucket = redissonClient.getBucket(SESSION_PREFIX + userId);
//        bucket.delete();
        bucket(SESSION_PREFIX + userId).delete();
    }

    // gen refresh token
    public String createRefreshToken(Long userId, String sid) {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        String tokenHash = hash(token);
        String redisKey = REFRESH_PREFIX + tokenHash;

//        RBucket<String> bucket = redissonClient.getBucket(redisKey);
        String value = userId + ":" + sid;

        bucket(redisKey).set(value, Duration.ofMillis(refreshExpirationMs));
        return token;
    }

    // delete refresh token
    public void deleteRefreshToken(String token) {
//        String tokenHash = hash(token);
//        String redisKey = REFRESH_PREFIX + tokenHash;
//        redissonClient.getBucket(redisKey).delete();
        bucket(REFRESH_PREFIX + hash(token)).delete();
    }

    // find refresh token
    public Optional<RefreshEntry> findRefreshToken(String token) {
        String tokenHash = hash(token);
        String redisKey = REFRESH_PREFIX + tokenHash;

//        RBucket<String> bucket = redissonClient.getBucket(redisKey);
        String value = bucket(redisKey).get();
        if (value == null) {
            return Optional.empty();
        }
        String[] parts = value.split(":", 2);
        return Optional.of(new RefreshEntry(Long.parseLong(parts[0]), parts[1]));
    }

    public record RefreshEntry(Long userId, String sid) {
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private RBucket<String> bucket(String key) {
        return redissonClient.getBucket(key, StringCodec.INSTANCE);
    }
}
