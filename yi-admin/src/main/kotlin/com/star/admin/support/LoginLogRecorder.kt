package com.star.admin.support

import com.star.identity.entity.LoginLogs
import com.star.identity.entity.by
import jakarta.servlet.http.HttpServletRequest
import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 登录日志直接写入 sys_login_log。只追加，不经过用户 DAO。
 * loginType：1 登录，2 登出。status：0 成功，1 失败。
 */
@Component
class LoginLogRecorder(
    private val sql: KSqlClient,
) {

    fun recordLoginSuccess(request: HttpServletRequest, userId: Long, username: String, token: String?) {
        save(request, userId, username, TYPE_LOGIN, STATUS_SUCCESS, null, token)
    }

    fun recordLoginFailure(
        request: HttpServletRequest,
        username: String,
        userId: Long?,
        failReason: String,
    ) {
        save(request, userId, username, TYPE_LOGIN, STATUS_FAILURE, failReason, null)
    }

    fun recordLogout(request: HttpServletRequest, userId: Long, username: String, token: String?) {
        save(request, userId, username, TYPE_LOGOUT, STATUS_SUCCESS, null, token)
    }

    private fun save(
        request: HttpServletRequest,
        userId: Long?,
        username: String,
        loginType: Int,
        status: Int,
        failReason: String?,
        token: String?,
    ) {
        val client = ClientInfo.from(request)
        val log = new(LoginLogs::class).by {
            if (userId != null) {
                this.userId = userId
            }
            this.username = username
            this.loginType = loginType
            this.status = status
            this.failReason = failReason
            ipAddress = client.ip
            browser = client.browser
            os = client.os
            userAgent = client.userAgent
            tokenValue = maskToken(token)
            loginTime = LocalDateTime.now()
        }
        sql.save(log) {
            setMode(SaveMode.INSERT_ONLY)
        }
    }

    private fun maskToken(token: String?): String? {
        if (token.isNullOrBlank()) {
            return null
        }
        if (token.length <= 12) {
            return token
        }
        return token.substring(0, 8) + "****"
    }

    private data class ClientInfo(
        val ip: String?,
        val browser: String?,
        val os: String?,
        val userAgent: String?,
    ) {
        companion object {
            fun from(request: HttpServletRequest?): ClientInfo {
                if (request == null) {
                    return ClientInfo(null, null, null, null)
                }
                val userAgent = request.getHeader("User-Agent")
                return ClientInfo(resolveIp(request), parseBrowser(userAgent), parseOs(userAgent), userAgent)
            }

            private fun resolveIp(request: HttpServletRequest): String {
                val forwarded = request.getHeader("X-Forwarded-For")
                if (!forwarded.isNullOrBlank()) {
                    val comma = forwarded.indexOf(',')
                    return if (comma > 0) forwarded.substring(0, comma).trim() else forwarded.trim()
                }
                val realIp = request.getHeader("X-Real-IP")
                if (!realIp.isNullOrBlank()) {
                    return realIp.trim()
                }
                return request.remoteAddr
            }

            private fun parseBrowser(userAgent: String?): String? {
                if (userAgent == null) {
                    return null
                }
                return when {
                    userAgent.contains("Edg/") -> "Edge"
                    userAgent.contains("Chrome/") -> "Chrome"
                    userAgent.contains("Firefox/") -> "Firefox"
                    userAgent.contains("Safari/") -> "Safari"
                    else -> "Other"
                }
            }

            private fun parseOs(userAgent: String?): String? {
                if (userAgent == null) {
                    return null
                }
                return when {
                    userAgent.contains("Windows") -> "Windows"
                    userAgent.contains("Mac OS") -> "macOS"
                    userAgent.contains("Android") -> "Android"
                    userAgent.contains("iPhone") || userAgent.contains("iPad") -> "iOS"
                    userAgent.contains("Linux") -> "Linux"
                    else -> "Other"
                }
            }
        }
    }

    companion object {
        const val TYPE_LOGIN = 1
        const val TYPE_LOGOUT = 2
        const val STATUS_SUCCESS = 0
        const val STATUS_FAILURE = 1
    }
}
