package com.star.admin.controller

import cn.dev33.satoken.util.SaResult
import com.star.admin.dao.UsersDao
import com.star.common.exception.BusinessException
import com.star.identity.entity.Users
import com.star.identity.entity.by
import org.babyfish.jimmer.sql.kt.fetcher.newFetcher
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 用户查询试接口。路径使用 test 前缀，当前不校验登录。
 * 返回字段由 USER_FETCHER 决定，不加载密码。
 */
@RestController
@RequestMapping("/test/users")
@Transactional(readOnly = true)
class UsersTestController(
    private val usersDao: UsersDao,
) {

    @GetMapping
    fun list(@RequestParam(required = false) username: String?): SaResult =
        SaResult.data(usersDao.list(username?.takeIf { it.isNotBlank() }, USER_FETCHER))

    @GetMapping("/{id}")
    fun findOne(@PathVariable id: Long): SaResult {
        val user = usersDao.findOne(id, USER_FETCHER)
            ?: throw BusinessException("用户不存在").code(404)
        return SaResult.data(user)
    }

    companion object {
        private val USER_FETCHER =
            newFetcher(Users::class).by {
                allScalarFields()
                password(false)
            }
    }
}
