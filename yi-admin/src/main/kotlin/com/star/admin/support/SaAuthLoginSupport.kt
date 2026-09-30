package com.star.admin.support

import cn.dev33.satoken.stp.StpUtil
import cn.dev33.satoken.stp.parameter.SaLoginParameter

/**
 * API 和后台页面使用不同的 deviceType，避免同一个浏览器里 Cookie 互相覆盖。
 *
 * createLoginSession 只创建会话并返回 token，不写 Cookie，也不写响应头。
 * StpUtil.login 会在创建会话之后把 token 写入 Cookie。API 登录不能走这条路径。
 */
object SaAuthLoginSupport {

    /** 前后端分离的 REST 接口。 */
    const val DEVICE_API = "api"

    /** Thymeleaf 后台页面。页面登录接上之后使用。 */
    const val DEVICE_ADMIN_WEB = "admin-web"

    /**
     * API 登录。token 放进 JSON，由前端放到 satoken 请求头。
     * isShare 为 false，这一次登录的 token 和其他端互不影响。
     */
    fun establishApiSession(userId: Long): String {
        val parameter = SaLoginParameter.create()
            .setDeviceType(DEVICE_API)
            .setIsShare(false)
        return StpUtil.createLoginSession(userId, parameter)
    }

    /**
     * 后台页面登录。走完整 login，token 写入 Cookie，不写响应头。
     */
    fun establishAdminWebSession(userId: Long) {
        StpUtil.login(
            userId,
            SaLoginParameter.create()
                .setDeviceType(DEVICE_ADMIN_WEB)
                .setIsLastingCookie(true)
                .setIsShare(false)
                .setIsWriteHeader(false),
        )
    }
}
