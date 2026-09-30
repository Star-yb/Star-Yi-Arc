package com.star.identity.entity

import com.fasterxml.jackson.annotation.JsonFormat
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ForeignKeyType
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime

/**
 * 登录日志，表 sys_login_log。只追加，不继承 BaseEntity，也不做逻辑删除。
 * loginType：1 登录，2 登出。status：0 成功，1 失败。
 * user 是假外键，用户被删后日志仍然保留。
 */
@Entity
@Table(name = "sys_login_log")
interface LoginLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKeyType = ForeignKeyType.FAKE)
    val user: Users?

    @IdView("user")
    val userId: Long?

    val username: String

    val loginType: Int

    val status: Int

    val failReason: String?

    val ipAddress: String?

    val loginLocation: String?

    val browser: String?

    val os: String?

    val userAgent: String?

    val tokenValue: String?

    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val loginTime: LocalDateTime
}
