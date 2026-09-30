package com.star.admin.dao

import com.star.admin.dto.UserCreateInputView
import com.star.admin.dto.UserRoleUpdateInputView
import com.star.admin.dto.UserUpdateInputView
import com.star.common.exception.BusinessException
import com.star.identity.entity.Users
import com.star.identity.entity.id
import com.star.identity.entity.lastLoginTime
import com.star.identity.entity.password
import com.star.identity.entity.status
import com.star.identity.entity.superAdmin
import com.star.identity.entity.username
import org.babyfish.jimmer.spring.repo.support.AbstractKotlinRepository
import org.babyfish.jimmer.spring.repository.fetchSpringPage
import org.babyfish.jimmer.spring.repository.orderBy
import org.babyfish.jimmer.sql.ast.mutation.DeleteMode
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.`eq?`
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.query.KConfigurableRootQuery
import org.babyfish.jimmer.sql.kt.ast.query.KMutableRootQuery
import org.babyfish.jimmer.sql.kt.ast.query.specification.KSpecification
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * 用户数据访问。查询条件写在 createQuery 里。
 * 分页方法先不实现，需要时再按下面预留的签名补。
 */
@Repository
class UsersDao(
    sql: KSqlClient,
) : AbstractKotlinRepository<Users, Long>(sql) {

    /**
     * 按用户名查列表。username 为空时不加这个条件。
     */
    fun list(username: String?, fetcher: Fetcher<Users>): List<Users> =
        createQuery {
            where(table.username `eq?` username)
            select(table.fetch(fetcher))
        }.execute()

    /**
     * 按主键查一条。不存在时返回 null。
     */
    fun findOne(id: Long, fetcher: Fetcher<Users>): Users? =
        findById(id, fetcher)

    /** superAdmin 为 1 时是超级管理员。用户不存在时返回 false。 */
    fun isSuperAdmin(id: Long): Boolean =
        createQuery {
            where(table.id eq id)
            select(table.superAdmin)
        }.fetchOneOrNull() == 1

    /**
     * 创建用户。用户名已存在时不写入，并返回 400。
     * fetcher 决定返回实体里加载哪些字段。
     */
    fun add(input: UserCreateInputView, fetcher: Fetcher<Users>?): Users {
        val result = saveCommand(input) {
            setMode(SaveMode.INSERT_IF_ABSENT)
        }.execute(fetcher)
        if (result.totalAffectedRowCount == 0) {
            throw BusinessException("用户名已存在").code(400)
        }
        return result.modifiedEntity
    }

    /**
     * 按主键更新用户。目标行不存在时返回 400。
     * 密码和状态不在这个入参里，分别走 updatePassword、updateStatus。
     */
    fun update(input: UserUpdateInputView, fetcher: Fetcher<Users>?): Users {
        val result = saveCommand(input) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute(fetcher)
        if (result.totalAffectedRowCount == 0) {
            throw BusinessException("要更新的数据不存在").code(400)
        }
        return result.modifiedEntity
    }

    /**
     * 记录最近登录时间。
     */
    fun updateLastLoginTime(id: Long) {
        executeUpdate {
            set(table.lastLoginTime, LocalDateTime.now())
            where(table.id eq id)
        }
    }

    /**
     * 写入已经加密过的密码。
     */
    fun updatePassword(id: Long, password: String) {
        executeUpdate {
            set(table.password, password)
            where(table.id eq id)
        }
    }

    /**
     * 逻辑删除。Users 带逻辑删除标记，普通查询不会再查出这一行。
     */
    fun delete(id: Long) {
        deleteById(id, DeleteMode.AUTO)
    }

    /**
     * 改状态。0 正常，1 禁用。
     */
    fun updateStatus(id: Long, status: Int) {
        executeUpdate {
            set(table.status, status)
            where(table.id eq id)
        }
    }

    /**
     * 按动态入参改用户角色。只提交了的字段才会写入。
     * 返回受影响的行数，含中间表。
     */
    fun updateRole(input: UserRoleUpdateInputView): Int =
        sql.save(input).totalAffectedRowCount

    /**
     * 按 Specification 查列表，不分页。specification 为空时不加条件。
     */
    fun queryList(specification: KSpecification<Users>?, fetcher: Fetcher<Users>?): List<Users> =
        createQuery {
            where(specification)
            select(table.fetch(fetcher))
        }.execute()

    /**
     * 自定义条件的列表查询，不分页。条件和返回列写在 block 里。
     */
    fun <R> queryList(
        block: KMutableRootQuery.ForEntity<Users>.() -> KConfigurableRootQuery<KNonNullTable<Users>, R>,
    ): List<R> = createQuery(block).execute()

    /**
     * 按 Specification 查一页。排序来自 Pageable，总数由 Jimmer 另查一次。
     */
    fun queryPage(
        pageable: Pageable,
        specification: KSpecification<Users>?,
        fetcher: Fetcher<Users>?,
    ): Page<Users> =
        createQuery {
            where(specification)
            orderBy(pageable.sort)
            select(table.fetch(fetcher))
        }.fetchSpringPage(pageable)

    /**
     * 物理删除，给超级管理员清数据。普通删除用 delete。
     */
    fun adminDelete(id: Long) {
        executeDelete {
            where(table.id eq id)
            setMode(DeleteMode.PHYSICAL)
        }
    }
}
