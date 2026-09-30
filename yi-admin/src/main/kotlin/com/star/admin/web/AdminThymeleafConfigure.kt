package com.star.admin.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver
import org.thymeleaf.templatemode.TemplateMode
import java.nio.charset.StandardCharsets

/**
 * 业务 JAR 可以把模板放在 classpath:/admin/。
 * 先找 admin/模块/页面.html，没有再回退到本模块的 templates。
 */
@Configuration
class AdminThymeleafConfigure {

    @Bean
    fun adminModuleTemplateResolver(): SpringResourceTemplateResolver {
        val resolver = SpringResourceTemplateResolver()
        resolver.setPrefix("classpath:/admin/")
        resolver.setSuffix(".html")
        resolver.templateMode = TemplateMode.HTML
        resolver.characterEncoding = StandardCharsets.UTF_8.name()
        resolver.setCheckExistence(true)
        resolver.order = 1
        resolver.name = "adminModuleTemplateResolver"
        return resolver
    }
}
