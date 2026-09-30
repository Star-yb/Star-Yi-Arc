package com.star.admin.controller.auth

import cn.dev33.satoken.stp.StpUtil
import cn.dev33.satoken.util.SaResult
import com.star.admin.dto.LoginInput
import com.star.admin.dto.LoginView
import com.star.admin.dto.VerifyInput
import com.star.admin.dto.VerifyView
import com.star.admin.service.AuthService
import com.star.admin.support.LoginLogRecorder
import com.star.admin.support.SaAuthLoginSupport
import com.star.common.exception.BusinessException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 认证接口，路径 /auth，不要求登录。
 * 登出自己判断有没有 token，只注销 api 端会话。
 */
@Tag(name = "认证", description = "登录、登出与 Token 校验，路径 /auth")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val loginLogRecorder: LoginLogRecorder,
) {

    @Operation(summary = "用户登录", description = "免鉴权；成功返回 satoken 及用户信息")
    @PostMapping("login")
    fun login(@RequestBody input: LoginInput, request: HttpServletRequest): SaResult {
        val username = input.username
        try {
            val user = authService.login(username, input.password)
            val token = SaAuthLoginSupport.establishApiSession(user.id)
            authService.onLoginSuccess(user.id)
            loginLogRecorder.recordLoginSuccess(request, user.id, username, token)
            val loginView = LoginView(user, token, StpUtil.getTokenTimeout(token))
            return SaResult.data(loginView)
        } catch (exception: BusinessException) {
            val userId = authService.resolveUserIdByLoginAccount(username)
            loginLogRecorder.recordLoginFailure(request, username, userId, exception.message ?: "登录失败")
            throw exception
        }
    }

    @Operation(summary = "用户登出", description = "仅注销 API 端会话，不影响后台页面 Cookie")
    @GetMapping("logout")
    fun logout(request: HttpServletRequest): SaResult {
        if (StpUtil.isLogin()) {
            val userId = StpUtil.getLoginIdAsLong()
            val username = authService.resolveLoginUsername(userId)
            loginLogRecorder.recordLogout(request, userId, username, StpUtil.getTokenValue())
            StpUtil.logout(userId, SaAuthLoginSupport.DEVICE_API)
        }
        return SaResult.ok()
    }

    @Operation(summary = "校验 Token", description = "免鉴权；根据 token 判断是否过期及关联用户")
    @PostMapping("verify")
    fun validateToken(@RequestBody input: VerifyInput): SaResult {
        val tokenValue = input.token
        if (tokenValue.isBlank()) {
            return SaResult.data(
                VerifyView(
                    tokenId = null,
                    token = tokenValue,
                    tokenTimeout = 0L,
                    expired = true,
                    userStatus = false,
                    userId = "",
                ),
            )
        }
        val loginId = StpUtil.getLoginIdByToken(tokenValue)
        val expired = loginId == null
        val userId = loginId?.toString()?.toLongOrNull()
        return SaResult.data(
            VerifyView(
                tokenId = loginId,
                token = tokenValue,
                tokenTimeout = StpUtil.getTokenTimeout(tokenValue),
                expired = expired,
                userStatus = userId != null && authService.isUserActive(userId),
                userId = userId?.toString() ?: "",
            ),
        )
    }
}
