package com.star.admin.web

/**
 * 超级管理员后台的侧栏扩展点。
 * 业务模块实现这个接口并注册成 Bean，启动时会被收进同一套侧栏。
 * 页面路径以 /admin/ 开头，当前页高亮和面包屑按这个路径匹配。
 * 分组按模块顺序出现。模板可以放在本模块的 templates，也可以放在依赖 JAR 的 classpath:/admin/。
 */
interface AdminPageModule {

    /** 模块标识，用于日志和排序。 */
    fun moduleId(): String

    /** 模块整体排序，越小越靠前。 */
    fun moduleOrder(): Int = 100

    /** 本模块贡献的侧栏菜单。 */
    fun menuItems(): List<AdminMenuItem>
}

/**
 * 一条侧栏菜单。menuKey 与页面里的 activeMenu 对应，用来高亮当前项。
 */
data class AdminMenuItem(
    val menuKey: String,
    val category: String,
    val label: String,
    val path: String,
    val icon: String,
    val order: Int,
)

/** 按分组标题聚合后的侧栏。 */
data class AdminMenuCategory(
    val name: String,
    val items: List<AdminMenuItem>,
)
