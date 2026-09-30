package com.star.admin.support

import com.star.common.page.PageQuery
import com.star.common.page.PageResult
import com.star.identity.entity.LoginLogs
import com.star.identity.entity.id
import com.star.identity.entity.loginTime
import com.star.identity.entity.loginType
import com.star.identity.entity.status
import com.star.identity.entity.username
import org.babyfish.jimmer.spring.repository.fetchSpringPage
import org.babyfish.jimmer.spring.repository.orderBy
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.desc
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.ilike
import org.babyfish.jimmer.sql.kt.ast.expression.lt
import org.babyfish.jimmer.sql.kt.ast.expression.sql
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.ast.expression.valueNotIn
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 登录日志的查询和清理，只给后台页面用。
 * 列表走 Jimmer 分页。没传排序时按登录时间倒序。
 */
@Component
class LoginLogSupport(
    private val sql: KSqlClient,
) {

    fun list(pageQuery: PageQuery, username: String?, loginType: Int?, status: Int?): PageResult<LoginLogs> {
        if (pageQuery.sort.isNullOrBlank()) {
            pageQuery.sort = "loginTime"
            pageQuery.order = "DESC"
        }
        val pageable = pageQuery.toPageable()
        val page = sql.createQuery(LoginLogs::class) {
            if (!username.isNullOrBlank()) {
                where(table.username ilike "%${username.trim()}%")
            }
            if (loginType != null) {
                where(table.loginType eq loginType)
            }
            if (status != null) {
                where(table.status eq status)
            }
            orderBy(pageable.sort)
            select(table)
        }.fetchSpringPage(pageable)
        return PageResult.ofPaged(page)
    }

    fun count(): Long =
        sql.createQuery(LoginLogs::class) {
            select(table)
        }.fetchUnlimitedCount()

    fun deleteByIds(ids: List<Long>): Int {
        if (ids.isEmpty()) {
            return 0
        }
        return sql.createDelete(LoginLogs::class) {
            where(table.id valueIn ids)
        }.execute()
    }

    fun deleteOlderThanDays(days: Int): Int {
        if (days <= 0) {
            return 0
        }
        val before = LocalDateTime.now().minusDays(days.toLong())
        return sql.createDelete(LoginLogs::class) {
            where(table.loginTime lt before)
        }.execute()
    }

    fun deleteKeepLatest(keepCount: Int): Int {
        if (keepCount <= 0) {
            return deleteAll()
        }
        val keepIds = sql.createQuery(LoginLogs::class) {
            orderBy(table.loginTime.desc())
            select(table.id)
        }.limit(keepCount).execute()
        if (keepIds.isEmpty() || count() <= keepCount) {
            return 0
        }
        return sql.createDelete(LoginLogs::class) {
            where(table.id valueNotIn keepIds)
        }.execute()
    }

    fun deleteAll(): Int =
        sql.createDelete(LoginLogs::class) {
            where(sql(Boolean::class, "1 = 1"))
        }.execute()
}
