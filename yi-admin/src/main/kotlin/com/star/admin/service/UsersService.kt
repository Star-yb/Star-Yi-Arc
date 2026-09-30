package com.star.admin.service

import com.star.admin.dto.UserCreateInputView
import com.star.admin.dto.UserRoleUpdateInputView
import com.star.admin.dto.UserSpecification
import com.star.admin.dto.UserUpdateInputView
import com.star.common.page.PageQuery
import com.star.common.page.PageResult
import com.star.identity.entity.Users

/**
 * 用户服务。列表接口分页，导出仍取全量。
 */
interface UsersService {

    fun createUser(user: UserCreateInputView): Users

    fun updateUser(user: UserUpdateInputView): Users

    fun deleteUser(id: Long)

    fun obtainUser(id: Long): Users?

    /** 按条件查一页。没传页码时为第 1 页，每页 10 条。 */
    fun listUsers(pageQuery: PageQuery, specification: UserSpecification?): PageResult<Users>

    /** 按条件查全部，给导出用。 */
    fun listUsers(specification: UserSpecification?): List<Users>

    /** 启用用户，状态改为 0。 */
    fun enableUser(id: Long)

    /** 禁用用户，状态改为 1。 */
    fun disableUser(id: Long)

    fun updateRole(input: UserRoleUpdateInputView): Int

    /** 校验原密码后写入新密码。 */
    fun changePassword(userId: Long, oldPassword: String, newPassword: String)

    /** 某个角色下的用户。 */
    fun getRoleUsers(roleId: Long): List<Users>

    fun adminDelete(id: Long)

    /** 重置为默认密码 123456。 */
    fun resetPassword(id: Long)
}
