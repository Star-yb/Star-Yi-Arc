package com.star.config.satoken

import cn.dev33.satoken.SaManager
import cn.dev33.satoken.context.SaHolder
import cn.dev33.satoken.filter.SaServletFilter
import cn.dev33.satoken.`fun`.SaFunction
import cn.dev33.satoken.interceptor.SaInterceptor
import cn.dev33.satoken.jwt.StpLogicJwtForSimple
import cn.dev33.satoken.router.SaHttpMethod
import cn.dev33.satoken.router.SaRouter
import cn.dev33.satoken.stp.StpLogic
import cn.dev33.satoken.stp.StpUtil
import cn.dev33.satoken.thymeleaf.dialect.SaTokenDialect
import cn.dev33.satoken.util.SaResult
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Sa-Token 在 yi-admin 里的启动装配。
 *
 * token 使用 JWT 简单模式，密钥、超时和 token 名在 application.properties 的 sa-token.* 里。
 * 拦截器对白名单以外的请求校验登录；admin 下的路径再要求角色 *。
 * Servlet Filter 打请求日志，把鉴权异常转成 SaResult，并补上跨域响应头。
 */
@Configuration
class SaTokenConfigure : WebMvcConfigurer {

    /** 用 JWT 承载登录态，不再把 token 存进 Redis 会话。 */
    @Bean
    fun getStpLogicJwt(): StpLogic = StpLogicJwtForSimple()

    /** 模板标签 sa:login、sa:hasRole 依赖这个方言。 */
    @Bean
    fun getSaTokenDialect(): SaTokenDialect = SaTokenDialect()

    /**
     * 全局登录拦截。/error 不进拦截器，避免异常页再次被鉴权拦住。
     * 白名单见 [excludePaths]。
     */
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(SaInterceptor {
            println("---------- Sa-Token 全局过滤器")
            SaRouter.match("/**")
                .notMatch(excludePaths())
                .check(SaFunction { StpUtil.checkLogin() })
            SaRouter.match("/admin/**", SaFunction { StpUtil.checkRole("*") })
        }).addPathPatterns("/**").excludePathPatterns("/error")
    }

    /**
     * 不要求登录的路径：静态资源、登录接口、测试接口、OpenAPI 文档。
     * 这些路径仍会经过下面的 Filter，只是不做 checkLogin。
     */
    fun excludePaths(): List<String> = listOf(
        "/static/**",
        "/**.png",
        "**/**.html",
        "**/**.css",
        "**/**.js",
        "/auth/**",
        "/test/**",
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
    )

    /**
     * 比拦截器更外层的过滤器。
     * setAuth 只记日志，真正的登录校验在拦截器里。
     * setError 把异常收成 SaResult，避免过滤器直接抛出堆栈。
     * setBeforeAuth 对所有请求加跨域头，OPTIONS 预检直接返回。
     */
    @Bean
    fun getSaServletFilter(): SaServletFilter {
        return SaServletFilter()
            .addInclude("/**")
            .addExclude("/favicon.ico")
            .setAuth {
                SaManager.getLog().debug(
                    "-----请求类型{} 请求path={}  提交token={}",
                    SaHolder.getRequest().method,
                    SaHolder.getRequest().requestPath,
                    StpUtil.getTokenValue(),
                )
            }
            .setError { exception ->
                println("---------- 异常处理")
                SaResult.error(exception.message)
            }
            .setBeforeAuth {
                SaHolder.getResponse()
                    .setHeader("Access-Control-Allow-Origin", "*")
                    .setHeader("Access-Control-Allow-Methods", "*")
                    .setHeader("Access-Control-Allow-Headers", "*")
                    .setHeader("Access-Control-Max-Age", "3600")
                SaRouter.match(SaHttpMethod.OPTIONS)
                    .free { println("--------OPTIONS预检请求，不做处理") }
                    .back()
            }
    }
}
