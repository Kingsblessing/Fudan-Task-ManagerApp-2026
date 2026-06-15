package com.example.taskmanager.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token 黑名单服务：登出时注销 Token，鉴权时检查是否已注销
 */
public interface TokenBlacklist {

    /**
     * 将 Token 加入黑名单
     */
    void blacklist(String jti, long expiresAtMillis);

    /**
     * 检查 Token 是否在黑名单中
     */
    boolean isBlacklisted(String jti);

    // ==================== 内存实现 ====================

    @Component
    @ConditionalOnProperty(name = "storage.type", havingValue = "memory", matchIfMissing = true)
    class MemoryTokenBlacklist implements TokenBlacklist {
        private static final Logger log = LoggerFactory.getLogger(MemoryTokenBlacklist.class);
        private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

        @Override
        public void blacklist(String jti, long expiresAtMillis) {
            blacklist.put(jti, expiresAtMillis);
            log.info("Token 加入黑名单(内存): jti={}", jti);
            // 清理已过期的条目
            long now = System.currentTimeMillis();
            blacklist.entrySet().removeIf(e -> e.getValue() < now);
        }

        @Override
        public boolean isBlacklisted(String jti) {
            Long expiresAt = blacklist.get(jti);
            if (expiresAt == null) return false;
            if (expiresAt < System.currentTimeMillis()) {
                blacklist.remove(jti);
                return false;
            }
            return true;
        }
    }

    // ==================== 数据库实现 ====================

    @Component
    @ConditionalOnProperty(name = "storage.type", havingValue = "db")
    class DbTokenBlacklist implements TokenBlacklist {
        private static final Logger log = LoggerFactory.getLogger(DbTokenBlacklist.class);
        private final JdbcTemplate jdbcTemplate;

        public DbTokenBlacklist(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        @Override
        public void blacklist(String jti, long expiresAtMillis) {
            jdbcTemplate.update(
                    "INSERT INTO token_blacklist (jti, expires_at) VALUES (?, ?) ON DUPLICATE KEY UPDATE expires_at = VALUES(expires_at)",
                    jti, new Timestamp(expiresAtMillis));
            log.info("Token 加入黑名单(DB): jti={}", jti);
        }

        @Override
        public boolean isBlacklisted(String jti) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM token_blacklist WHERE jti = ? AND expires_at > NOW()",
                    Integer.class, jti);
            return count != null && count > 0;
        }
    }
}
