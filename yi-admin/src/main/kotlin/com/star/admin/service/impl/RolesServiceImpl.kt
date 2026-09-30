package com.star.admin.service.impl

import com.star.admin.auth.SaAuthCache
import com.star.admin.dao.RolesDao
import com.star.admin.dto.RoleCreateInputView
import com.star.admin.dto.RolePermissionUpdateInputView
import com.star.admin.dto.RoleSpecification
import com.star.admin.dto.RoleUpdateInputView
import com.star.admin.dto.RolesView
import com.star.admin.service.RolesService
import com.star.common.page.PageQuery
import com.star.common.page.PageResult
import com.star.identity.entity.Roles
import com.star.identity.entity.id
import com.star.identity.entity.users
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.stereotype.Service

@Service
class RolesServiceImpl(
    private val rolesDao: RolesDao,
) : RolesService {

    override fun createRole(role: RoleCreateInputView): Roles =
        rolesDao.add(role, ROLE_FETCHER)

    override fun updateRole(role: RoleUpdateInputView): Roles {
        val previousCode = role.id?.let { rolesDao.findById(it)?.roleCode }
        val updated = rolesDao.update(role, ROLE_FETCHER)
        SaAuthCache.evictRolePermissions(updated.roleCode)
        if (previousCode != null && previousCode != updated.roleCode) {
            SaAuthCache.evictRolePermissions(previousCode)
            SaAuthCache.evictAllLoginRoles()
        }
        return updated
    }

    override fun deleteRole(id: Long) {
        val role = rolesDao.findById(id) ?: return
        SaAuthCache.evictRolePermissions(role.roleCode)
        SaAuthCache.evictAllLoginRoles()
        rolesDao.delete(id)
    }

    override fun obtainRole(id: Long): Roles? =
        rolesDao.findById(id, ROLE_FETCHER)

    override fun listRoles(pageQuery: PageQuery, specification: RoleSpecification?): PageResult<Roles> =
        PageResult.ofPaged(rolesDao.queryPage(pageQuery.toPageable(), specification, ROLE_FETCHER))

    override fun listRoles(specification: RoleSpecification?): List<Roles> =
        rolesDao.queryList(specification, ROLE_FETCHER)

    override fun enableRole(id: Long) {
        val roleCode = rolesDao.findById(id)?.roleCode
        rolesDao.updateStatus(id, 0)
        if (roleCode != null) {
            SaAuthCache.evictRolePermissions(roleCode)
            SaAuthCache.evictAllLoginRoles()
        }
    }

    override fun disableRole(id: Long) {
        val roleCode = rolesDao.findById(id)?.roleCode
        rolesDao.updateStatus(id, 1)
        if (roleCode != null) {
            SaAuthCache.evictRolePermissions(roleCode)
            SaAuthCache.evictAllLoginRoles()
        }
    }

    override fun updatePermission(input: RolePermissionUpdateInputView): Int {
        val affected = rolesDao.updatePermission(input)
        val roleCode = input.id?.let { rolesDao.findById(it)?.roleCode }
        if (roleCode != null) {
            SaAuthCache.evictRolePermissions(roleCode)
        }
        return affected
    }

    override fun getUserRoles(userId: Long): List<Roles> =
        rolesDao.queryList {
            where(table.users { id eq userId })
            select(table.fetch(ROLE_FETCHER))
        }

    override fun adminDelete(id: Long) {
        val roleCode = rolesDao.findById(id)?.roleCode
        rolesDao.adminDelete(id)
        if (roleCode != null) {
            SaAuthCache.evictRolePermissions(roleCode)
            SaAuthCache.evictAllLoginRoles()
        }
    }

    companion object {
        private val ROLE_FETCHER = RolesView.METADATA.fetcher
    }
}
