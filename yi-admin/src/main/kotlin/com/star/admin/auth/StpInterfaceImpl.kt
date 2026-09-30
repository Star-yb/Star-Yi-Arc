package com.star.admin.auth

import cn.dev33.satoken.stp.StpInterface
import com.star.admin.dao.PermissionsDao
import com.star.admin.dao.RolesDao
import com.star.admin.dao.UsersDao
import org.springframework.stereotype.Component

/**
 * 登录后的角色和权限来源。
 * 超级管理员（superAdmin 为 1）固定返回 *。
 * 普通账号只取状态为 0 的角色和权限，禁用后下一次鉴权就不再命中。
 */
@Component
class StpInterfaceImpl(
    private val usersDao: UsersDao,
    private val rolesDao: RolesDao,
    private val permissionsDao: PermissionsDao,
) : StpInterface {

    override fun getPermissionList(loginId: Any, loginType: String): List<String> {
        val roleCodes = getRoleList(loginId, loginType)
        if (roleCodes.isEmpty()) {
            return emptyList()
        }
        if ("*" in roleCodes) {
            return listOf("*")
        }
        return roleCodes.flatMap { roleCode -> permissionCodes(roleCode) }.distinct()
    }

    override fun getRoleList(loginId: Any, loginType: String): List<String> {
        val userId = loginId.toString().toLong()
        if (usersDao.isSuperAdmin(userId)) {
            val superRole = listOf("*")
            SaAuthCache.putStrings(SaAuthCache.loginRoleKey(loginId), superRole)
            return superRole
        }
        val cacheKey = SaAuthCache.loginRoleKey(loginId)
        SaAuthCache.getStrings(cacheKey)?.let { return it }
        val roleCodes = rolesDao.listRoleCodesByUserId(userId)
        SaAuthCache.putStrings(cacheKey, roleCodes)
        return roleCodes
    }

    private fun permissionCodes(roleCode: String): List<String> {
        val cacheKey = SaAuthCache.rolePermissionKey(roleCode)
        SaAuthCache.getStrings(cacheKey)?.let { return it }
        val codes = permissionsDao.listPermissionCodesByRoleCode(roleCode)
        SaAuthCache.putStrings(cacheKey, codes)
        return codes
    }
}
