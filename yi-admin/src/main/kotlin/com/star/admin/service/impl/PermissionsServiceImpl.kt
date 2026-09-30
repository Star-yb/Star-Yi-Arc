package com.star.admin.service.impl

import com.star.admin.auth.SaAuthCache
import com.star.admin.dao.PermissionsDao
import com.star.admin.dto.PermissionCreateInputView
import com.star.admin.dto.PermissionSpecification
import com.star.admin.dto.PermissionUpdateInputView
import com.star.admin.dto.PermissionsTreeView
import com.star.admin.dto.PermissionsView
import com.star.admin.service.PermissionsService
import com.star.common.page.PageQuery
import com.star.common.page.PageResult
import com.star.identity.entity.Permissions
import com.star.identity.entity.by
import com.star.identity.entity.id
import com.star.identity.entity.parentId
import com.star.identity.entity.roles
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.stereotype.Service

@Service
class PermissionsServiceImpl(
    private val permissionsDao: PermissionsDao,
) : PermissionsService {

    override fun createPermission(permission: PermissionCreateInputView): Permissions {
        val creating = permission.copy(parentId = permission.parentId ?: 0L)
        return permissionsDao.add(creating, PERMISSION_FETCHER)
    }

    override fun updatePermission(permission: PermissionUpdateInputView): Permissions {
        val updated = permissionsDao.update(permission, PERMISSION_FETCHER)
        SaAuthCache.evictAllRolePermissions()
        return updated
    }

    override fun deletePermission(id: Long) {
        permissionsDao.delete(id)
        SaAuthCache.evictAllRolePermissions()
    }

    override fun obtainPermission(id: Long): Permissions? =
        permissionsDao.findById(id, PERMISSION_FETCHER)

    override fun listPermissions(
        pageQuery: PageQuery,
        specification: PermissionSpecification?,
    ): PageResult<Permissions> =
        PageResult.ofPaged(permissionsDao.queryPage(pageQuery.toPageable(), specification, PERMISSION_FETCHER))

    override fun listPermissions(specification: PermissionSpecification?): List<Permissions> =
        permissionsDao.queryList(specification, PERMISSION_FETCHER)

    override fun getChildPermissions(parentId: Long): List<Permissions> =
        permissionsDao.queryList {
            where(table.parentId eq parentId)
            select(table.fetch(CHILD_FETCHER))
        }

    override fun getPermissionsTree(
        pageQuery: PageQuery,
        specification: PermissionSpecification?,
    ): PageResult<Permissions> =
        PageResult.ofPaged(permissionsDao.queryPage(pageQuery.toPageable(), specification, TREE_FETCHER))

    override fun enablePermission(id: Long) {
        permissionsDao.updateStatus(id, 0)
        SaAuthCache.evictAllRolePermissions()
    }

    override fun disablePermission(id: Long) {
        permissionsDao.updateStatus(id, 1)
        SaAuthCache.evictAllRolePermissions()
    }

    override fun getPermissionRoles(roleId: Long): List<Permissions> =
        permissionsDao.queryList {
            where(table.roles { id eq roleId })
            select(table.fetch(PERMISSION_FETCHER))
        }

    override fun adminDelete(id: Long) {
        permissionsDao.adminDelete(id)
        SaAuthCache.evictAllRolePermissions()
    }

    companion object {
        private val PERMISSION_FETCHER: Fetcher<Permissions> = PermissionsView.METADATA.fetcher

        private val TREE_FETCHER: Fetcher<Permissions> = PermissionsTreeView.METADATA.fetcher.add("parentId")

        private val CHILD_FETCHER: Fetcher<Permissions> =
            newFetcher(Permissions::class).by {
                allScalarFields()
                `parent*`()
                `childPermissions*`()
            }
    }
}
