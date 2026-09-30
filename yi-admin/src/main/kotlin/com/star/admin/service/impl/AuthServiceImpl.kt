package com.star.admin.service.impl

import com.star.admin.dao.UsersDao
import com.star.admin.service.AuthService
import com.star.common.exception.BusinessException
import com.star.identity.entity.Users
import com.star.identity.entity.by
import com.star.identity.entity.id
import com.star.identity.entity.phone
import com.star.identity.entity.username
import com.star.utils.AuthCryptoUtils
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.or
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.stereotype.Service

@Service
class AuthServiceImpl(
    private val usersDao: UsersDao,
) : AuthService {

    override fun login(username: String, password: String): Users {
        val user = findByAccount(username)
            ?: throw BusinessException("账号不存在").code(401)
        if (!AuthCryptoUtils.matches(password, user.password)) {
            throw BusinessException("账号或密码错误").code(401)
        }
        if (user.status != 0) {
            throw BusinessException("账号已禁用").code(401)
        }
        return user
    }

    override fun onLoginSuccess(userId: Long) {
        usersDao.updateLastLoginTime(userId)
    }

    override fun isUserActive(userId: Long): Boolean {
        val user = usersDao.findById(userId)
        return user != null && user.status == 0
    }

    override fun resolveUserIdByLoginAccount(account: String): Long? =
        usersDao.queryList {
            where(or(table.username eq account, table.phone eq account))
            select(table.id)
        }.firstOrNull()

    override fun resolveLoginUsername(userId: Long): String =
        usersDao.findById(userId)?.username ?: userId.toString()

    private fun findByAccount(account: String): Users? =
        usersDao.queryList {
            where(or(table.username eq account, table.phone eq account))
            select(table.fetch(LOGIN_FETCHER))
        }.firstOrNull()

    companion object {
        private val LOGIN_FETCHER =
            newFetcher(Users::class).by {
                username()
                password()
                status()
            }
    }
}
