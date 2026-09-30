package com.star.config.satoken

import cn.dev33.satoken.dao.SaTokenDao
import cn.dev33.satoken.dao.SaTokenDaoForRedisTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * 1.46 的 update 使用 SET KEEPTTL，要求 Redis 6.0 及以上。
 * 当前 Redis 更低，按官方兼容写法先读剩余 TTL，再按原过期时间写回。
 * 极端并发下过期时间可能有毫秒级偏差。
 *
 * @see <a href="https://sa-token.com/up/integ-redis.html">Sa-Token 集成 Redis</a>
 */
@Configuration
class SaTokenDaoConfigure {

    @Bean
    @Primary
    fun saTokenDao(): SaTokenDao =
        object : SaTokenDaoForRedisTemplate() {
            override fun update(key: String, value: String) {
                val finalKey = wrapKey(key)
                val expireMs = stringRedisTemplate.getExpire(finalKey, TimeUnit.MILLISECONDS)
                if (expireMs == SaTokenDao.NOT_VALUE_EXPIRE) {
                    return
                }
                if (expireMs == SaTokenDao.NEVER_EXPIRE) {
                    stringRedisTemplate.opsForValue().set(finalKey, value)
                } else {
                    stringRedisTemplate.opsForValue().set(finalKey, value, Duration.ofMillis(expireMs))
                }
            }
        }
}
