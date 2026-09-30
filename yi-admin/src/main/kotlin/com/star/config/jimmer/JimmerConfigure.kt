package com.star.config.jimmer

import org.babyfish.jimmer.sql.meta.DatabaseNamingStrategy
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Jimmer 在 yi-admin 里的启动装配。
 * 方言、SQL 日志、校验模式写在 application.properties 的 jimmer.* 里，这里只放代码才能决定的策略。
 */
@Configuration
class JimmerConfigure {

    /**
     * 列名策略：小写加下划线。实体属性 createdTime 对应列 created_time。
     * 显式写了 @Column(name = "...") 的字段不受这个策略影响。
     */
    @Bean
    fun databaseNamingStrategy(): DatabaseNamingStrategy = DefaultDatabaseNamingStrategy.LOWER_CASE
}
