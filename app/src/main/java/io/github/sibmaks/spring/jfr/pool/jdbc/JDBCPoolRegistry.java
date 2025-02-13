package io.github.sibmaks.spring.jfr.pool.jdbc;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sibmaks
 * @since 0.0.16
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JDBCPoolRegistry {
    private static final Map<Object, String> poolIds = new ConcurrentHashMap<>();

    public static String getPoolId(Object pool) {
        return poolIds.computeIfAbsent(pool, it -> UUID.randomUUID().toString());
    }
}
