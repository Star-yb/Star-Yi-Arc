package com.star.admin.auth

import cn.dev33.satoken.SaManager

/**
 * StpInterface 使用的 Redis 缓存。
 * 角色码按登录 id 缓存，权限码按角色编码缓存。改角色、权限或用户角色后要删掉对应键。
 */
object SaAuthCache {

    private const val ROLE_PERMISSION_PREFIX = "satoken:role-find-permission:"
    private const val LOGIN_ROLE_PREFIX = "satoken:loginId-find-role:"
    private const val TTL_SECONDS = 60L * 60 * 24 * 30

    fun rolePermissionKey(roleCode: String): String = ROLE_PERMISSION_PREFIX + roleCode

    fun loginRoleKey(loginId: Any): String = LOGIN_ROLE_PREFIX + loginId

    fun getStrings(key: String): List<String>? {
        val value = SaManager.getSaTokenDao().getObject(key) ?: return null
        val raw = value as? List<*> ?: return null
        return raw.map { it.toString() }
    }

    fun putStrings(key: String, values: List<String>) {
        SaManager.getSaTokenDao().setObject(key, values, TTL_SECONDS)
    }

    fun evictRolePermissions(roleCode: String) {
        SaManager.getSaTokenDao().deleteObject(rolePermissionKey(roleCode))
    }

    fun evictLoginRoles(loginId: Any) {
        SaManager.getSaTokenDao().deleteObject(loginRoleKey(loginId))
    }

    fun evictAllRolePermissions() {
        evictByPrefix(ROLE_PERMISSION_PREFIX)
    }

    fun evictAllLoginRoles() {
        evictByPrefix(LOGIN_ROLE_PREFIX)
    }

    private fun evictByPrefix(prefix: String) {
        val dao = SaManager.getSaTokenDao()
        dao.searchData(prefix, null, 0, 10_000, false).forEach { dao.deleteObject(it) }
    }
}
