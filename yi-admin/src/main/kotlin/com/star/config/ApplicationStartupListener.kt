package com.star.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

/**
 * 应用完全就绪后输出启动摘要，在 Tomcat 启动、Bean 初始化完成之后。
 */
@Component
class ApplicationStartupListener(
    private val environment: Environment,
) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        val port = resolvePort()
        val contextPath = resolveContextPath()
        val baseUrl = "http://localhost:$port$contextPath"
        val appName = environment.getProperty("spring.application.name", "application")

        log.info(
            "\n" + """
            ----------------------------------------------------------
              启动完毕 · {}
              API 根地址:     {}
              Swagger 文档:   {}/swagger-ui.html
              管理后台:       {}/admin/login
            ----------------------------------------------------------
            """.trimIndent(),
            appName,
            baseUrl,
            baseUrl,
            baseUrl,
        )
    }

    /**
     * 优先取 Spring Boot 启动后写入的运行时端口 local.server.port，与 Tomcat 日志一致。
     * 取不到时再读配置里的 server.port。
     */
    private fun resolvePort(): Int {
        val port = environment.getProperty("local.server.port", Int::class.java)
        if (port != null) {
            return port
        }
        return environment.getRequiredProperty("server.port", Int::class.java)
    }

    /** 从 server.servlet.context-path 读取。根路径不拼进地址。 */
    private fun resolveContextPath(): String {
        val contextPath = environment.getProperty("server.servlet.context-path", "")
        if (contextPath.isBlank() || contextPath == "/") {
            return ""
        }
        return contextPath.removeSuffix("/")
    }

    companion object {
        private val log = LoggerFactory.getLogger(ApplicationStartupListener::class.java)
    }
}
