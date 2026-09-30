package com.star.admin.web

import org.springframework.stereotype.Component

/**
 * yi-admin 自带的系统管理菜单。
 */
@Component
class SysAdminPageModule : AdminPageModule {

    override fun moduleId(): String = "yi-admin-sys"

    override fun moduleOrder(): Int = 0

    override fun menuItems(): List<AdminMenuItem> = listOf(
        item("user", "系统管理", "用户管理", "/admin/user", "bx bx-user", 10),
        item("role", "系统管理", "角色管理", "/admin/role", "bx bx-shield-quarter", 20),
        item("permission", "系统管理", "权限管理", "/admin/permission", "bx bx-lock-alt", 30),
        item("login-log", "系统监控", "登录日志", "/admin/login-log", "bx bx-log-in-circle", 10),
        item("api-docs", "开发工具", "接口文档", "/admin/api-docs", "bx bx-book-open", 10),
    )

    private fun item(
        menuKey: String,
        category: String,
        label: String,
        path: String,
        icon: String,
        order: Int,
    ) = AdminMenuItem(menuKey, category, label, path, icon, order)
}
