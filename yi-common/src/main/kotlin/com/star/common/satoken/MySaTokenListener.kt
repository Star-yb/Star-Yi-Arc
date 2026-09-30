package com.star.common.satoken

import cn.dev33.satoken.listener.SaTokenListenerForSimple
import cn.dev33.satoken.stp.parameter.SaLoginParameter
import org.springframework.stereotype.Component

/**
 * Sa-Token 登录事件监听。账号登录成功时进入 doLogin。
 * 目前只打一条日志，后面可以在这里记登录日志或更新最后登录时间。
 */
@Component
class MySaTokenListener : SaTokenListenerForSimple() {

    override fun doLogin(
        loginType: String,
        loginId: Any,
        tokenValue: String,
        loginParameter: SaLoginParameter,
    ) {
        println("---------- 自定义侦听器实现 doLogin")
    }
}
