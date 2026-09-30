package com.star.admin.web

import jakarta.servlet.http.HttpServletRequest
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

/**
 * 给 /admin 下的页面注入侧栏，并按当前路径标出正在看的菜单。登录页不注入。
 */
@ControllerAdvice
class AdminMenuModelAdvice(
    private val adminMenuRegistry: AdminMenuRegistry,
) {

    @ModelAttribute
    fun enrichAdminMenus(request: HttpServletRequest, model: Model) {
        val path = requestPath(request)
        if (!path.startsWith("/admin") || path == "/admin/login") {
            return
        }
        model.addAttribute("adminMenuCategories", adminMenuRegistry.getMenuCategories())
        model.addAttribute("currentAdminMenu", adminMenuRegistry.currentItem(path))
    }

    private fun requestPath(request: HttpServletRequest): String {
        val uri = request.requestURI ?: return ""
        val contextPath = request.contextPath ?: ""
        return if (contextPath.isNotEmpty() && uri.startsWith(contextPath)) {
            uri.removePrefix(contextPath)
        } else {
            uri
        }
    }
}
