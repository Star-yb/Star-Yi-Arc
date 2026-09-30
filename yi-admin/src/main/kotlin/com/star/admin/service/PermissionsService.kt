package com.star.admin.service

import com.star.admin.dto.PermissionCreateInputView
import com.star.admin.dto.PermissionSpecification
import com.star.admin.dto.PermissionUpdateInputView
import com.star.common.page.PageQuery
import com.star.common.page.PageResult
import com.star.identity.entity.Permissions

/**
 * 权限服务。列表和权限树分页，勾选页仍取全量。
 */
interface PermissionsService {

    fun createPermission(permission: PermissionCreateInputView): Permissions

    fun updatePermission(permission: PermissionUpdateInputView): Permissions

    fun deletePermission(id: Long)

    fun obtainPermission(id: Long): Permissions?

    /** 按条件查一页。没传页码时为第 1 页，每页 10 条。 */
    fun listPermissions(pageQuery: PageQuery, specification: PermissionSpecification?): PageResult<Permissions>

    /** 按条件查全部，给角色权限勾选页用。 */
    fun listPermissions(specification: PermissionSpecification?): List<Permissions>

    /** 某个父节点下的直接子权限，带子树字段。 */
    fun getChildPermissions(parentId: Long): List<Permissions>

    /** 按条件查一页权限树。 */
    fun getPermissionsTree(pageQuery: PageQuery, specification: PermissionSpecification?): PageResult<Permissions>

    /** 启用权限，状态改为 0。 */
    fun enablePermission(id: Long)

    /** 禁用权限，状态改为 1。 */
    fun disablePermission(id: Long)

    /** 某个角色已绑定的权限。 */
    fun getPermissionRoles(roleId: Long): List<Permissions>

    fun adminDelete(id: Long)
}
