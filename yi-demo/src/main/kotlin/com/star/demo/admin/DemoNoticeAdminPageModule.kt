package com.star.demo.admin

import com.star.admin.web.AdminMenuItem
import com.star.admin.web.AdminPageModule
import org.springframework.stereotype.Component

/** 向超级管理员后台注册演示公告菜单。 */
@Component
class DemoNoticeAdminPageModule : AdminPageModule {

    override fun moduleId(): String = "yi-demo"

    override fun moduleOrder(): Int = 90

    override fun menuItems(): List<AdminMenuItem> = listOf(
        AdminMenuItem(
            menuKey = "demo-notice",
            category = "开发演示",
            label = "演示公告",
            path = "/admin/demo/notices",
            icon = "bx bx-news",
            order = 10,
        ),
    )
}
