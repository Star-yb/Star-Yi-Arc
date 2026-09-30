package com.star.config.satoken

import cn.dev33.satoken.stp.StpUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.thymeleaf.spring6.view.ThymeleafViewResolver

/**
 * 把 Sa-Token 的 StpLogic 注册成 Thymeleaf 静态变量 stp。
 * 页面里可以直接写 stp.isLogin、stp.hasRole，不必每个 Controller 再往模型里塞一遍。
 */
@Configuration
class SaTokenThymeleafConfigure {

    @Autowired
    fun configureThymeleafStaticVars(viewResolver: ThymeleafViewResolver) {
        viewResolver.addStaticVariable("stp", StpUtil.stpLogic)
    }
}
