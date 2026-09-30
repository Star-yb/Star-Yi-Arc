package com.star.admin.service

import com.star.admin.dto.RoleCreateInputView
import com.star.admin.dto.RolePermissionUpdateInputView
import com.star.admin.dto.RoleSpecification
import com.star.admin.dto.RoleUpdateInputView
import com.star.common.page.PageQuery
import com.star.common.page.PageResult
import com.star.identity.entity.Roles

/**
 * 角色服务。列表接口分页，下拉框仍取全量。
 */
interface RolesService {

    fun createRole(role: RoleCreateInputView): Roles

    fun updateRole(role: RoleUpdateInputView): Roles

    fun deleteRole(id: Long)

    fun obtainRole(id: Long): Roles?

    /** 按条件查一页。没传页码时为第 1 页，每页 10 条。 */
    fun listRoles(pageQuery: PageQuery, specification: RoleSpecification?): PageResult<Roles>

    /** 按条件查全部，给用户页的角色下拉框用。 */
    fun listRoles(specification: RoleSpecification?): List<Roles>

    /** 启用角色，状态改为 0。 */
    fun enableRole(id: Long)

    /** 禁用角色，状态改为 1。 */
    fun disableRole(id: Long)

    /** 覆盖角色绑定的权限。返回受影响行数。 */
    fun updatePermission(input: RolePermissionUpdateInputView): Int

    /** 某个用户拥有的角色。 */
    fun getUserRoles(userId: Long): List<Roles>

    fun adminDelete(id: Long)
}
