package com.star.admin.web

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 收集所有 [AdminPageModule]，合并成侧栏分组。
 */
@Component
class AdminMenuRegistry(
    modules: List<AdminPageModule>,
) {
    private val modules: List<AdminPageModule> = modules

    init {
        this.modules.forEach { module ->
            log.info("已注册超级管理员页面模块: {} (order={})", module.moduleId(), module.moduleOrder())
        }
    }

    /** 分组按模块出现的顺序。同一分组里的菜单再按 order。 */
    fun getMenuCategories(): List<AdminMenuCategory> {
        val grouped = linkedMapOf<String, MutableList<AdminMenuItem>>()
        for (module in orderedModules()) {
            for (item in module.menuItems().sortedBy { it.order }) {
                grouped.getOrPut(item.category) { mutableListOf() }.add(item)
            }
        }
        return grouped.map { (name, items) ->
            AdminMenuCategory(name, items.sortedBy { it.order })
        }
    }

    /**
     * 用当前路径找菜单。完全相同优先；否则取最长的路径前缀。
     * /admin/role-permission 会落到 /admin/role。
     */
    fun currentItem(path: String): AdminMenuItem? =
        orderedModules()
            .flatMap { it.menuItems() }
            .filter { item -> path == item.path || path.startsWith(item.path) }
            .maxByOrNull { it.path.length }

    private fun orderedModules(): List<AdminPageModule> =
        modules.sortedWith(compareBy<AdminPageModule> { it.moduleOrder() }.thenBy { it.moduleId() })

    companion object {
        private val log = LoggerFactory.getLogger(AdminMenuRegistry::class.java)
    }
}
