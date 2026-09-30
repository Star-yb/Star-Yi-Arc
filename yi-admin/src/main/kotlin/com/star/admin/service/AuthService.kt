package com.star.admin.service

import com.star.identity.entity.Users

/**
 * 登录校验和登录后的账号查询。会话和登录日志不在这里。
 */
interface AuthService {

    /** 校验账号、密码和状态。账号可以是用户名或手机号。 */
    fun login(username: String, password: String): Users

    /** 会话建立成功后更新最后登录时间。 */
    fun onLoginSuccess(userId: Long)

    /** status 为 0 才算可登录。 */
    fun isUserActive(userId: Long): Boolean

    /** 按用户名或手机号找用户 id。账号不存在时返回 null。 */
    fun resolveUserIdByLoginAccount(account: String): Long?

    /** 登出日志里展示的账号。用户已不存在时退回用户 id。 */
    fun resolveLoginUsername(userId: Long): String
}
