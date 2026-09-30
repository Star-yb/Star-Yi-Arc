package com.star.admin.service.impl

import com.star.admin.auth.SaAuthCache
import com.star.admin.dao.UsersDao
import com.star.admin.dto.UserCreateInputView
import com.star.admin.dto.UserRoleUpdateInputView
import com.star.admin.dto.UserSpecification
import com.star.admin.dto.UserUpdateInputView
import com.star.admin.dto.UsersView
import com.star.admin.service.UsersService
import com.star.common.exception.BusinessException
import com.star.common.page.PageQuery
import com.star.common.page.PageResult
import com.star.identity.entity.Users
import com.star.identity.entity.by
import com.star.identity.entity.id
import com.star.identity.entity.roles
import com.star.utils.AuthCryptoUtils
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.stereotype.Service

/**
 * 用户服务实现。返回字段用 UsersView 的 Fetcher，列表额外带上 roleIds。
 */
@Service
class UsersServiceImpl(
    private val usersDao: UsersDao,
) : UsersService {

    override fun createUser(user: UserCreateInputView): Users =
        usersDao.add(user, USER_FETCHER)

    override fun updateUser(user: UserUpdateInputView): Users =
        usersDao.update(user, USER_FETCHER)

    override fun deleteUser(id: Long) {
        usersDao.delete(id)
        SaAuthCache.evictLoginRoles(id)
    }

    override fun obtainUser(id: Long): Users? =
        usersDao.findById(id, USER_FETCHER)

    override fun listUsers(pageQuery: PageQuery, specification: UserSpecification?): PageResult<Users> =
        PageResult.ofPaged(usersDao.queryPage(pageQuery.toPageable(), specification, USER_WITH_ROLE_IDS))

    /** 导出用，一次取出全部命中行。 */
    override fun listUsers(specification: UserSpecification?): List<Users> =
        usersDao.queryList(specification, USER_WITH_ROLE_IDS)

    override fun enableUser(id: Long) {
        usersDao.updateStatus(id, 0)
    }

    override fun disableUser(id: Long) {
        usersDao.updateStatus(id, 1)
    }

    override fun changePassword(userId: Long, oldPassword: String, newPassword: String) {
        val user = usersDao.findById(userId, PASSWORD_FETCHER)
            ?: throw BusinessException("用户不存在").code(404)
        if (!AuthCryptoUtils.matches(oldPassword, user.password)) {
            throw BusinessException("原密码不正确").code(400)
        }
        if (newPassword.length < 6) {
            throw BusinessException("新密码不能为空，长度至少为6").code(400)
        }
        usersDao.updatePassword(userId, AuthCryptoUtils.hash(newPassword))
    }

    override fun getRoleUsers(roleId: Long): List<Users> =
        usersDao.queryList {
            where(table.roles { id eq roleId })
            select(table.fetch(USER_FETCHER))
        }

    override fun updateRole(input: UserRoleUpdateInputView): Int {
        val affected = usersDao.updateRole(input)
        input.id?.let { SaAuthCache.evictLoginRoles(it) }
        return affected
    }

    override fun adminDelete(id: Long) {
        usersDao.adminDelete(id)
        SaAuthCache.evictLoginRoles(id)
    }

    override fun resetPassword(id: Long) {
        usersDao.updatePassword(id, AuthCryptoUtils.hash("123456"))
    }

    companion object {
        private val USER_FETCHER = UsersView.METADATA.fetcher

        private val USER_WITH_ROLE_IDS = UsersView.METADATA.fetcher.add("roleIds")

        private val PASSWORD_FETCHER =
            newFetcher(Users::class).by {
                password()
            }
    }
}
