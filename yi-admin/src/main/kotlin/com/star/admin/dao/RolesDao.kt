package com.star.admin.dao

import com.star.admin.dto.RoleCreateInputView
import com.star.admin.dto.RolePermissionUpdateInputView
import com.star.admin.dto.RoleSpecification
import com.star.admin.dto.RoleUpdateInputView
import com.star.common.exception.BusinessException
import com.star.identity.entity.Roles
import com.star.identity.entity.id
import com.star.identity.entity.roleCode
import com.star.identity.entity.status
import com.star.identity.entity.users
import org.babyfish.jimmer.spring.repo.support.AbstractKotlinRepository
import org.babyfish.jimmer.spring.repository.fetchSpringPage
import org.babyfish.jimmer.spring.repository.orderBy
import org.babyfish.jimmer.sql.ast.mutation.DeleteMode
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.query.KConfigurableRootQuery
import org.babyfish.jimmer.sql.kt.ast.query.KMutableRootQuery
import org.babyfish.jimmer.sql.kt.ast.query.specification.KSpecification
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

/**
 * 角色数据访问。查询条件写在 createQuery 里。
 */
@Repository
class RolesDao(
    sql: KSqlClient,
) : AbstractKotlinRepository<Roles, Long>(sql) {

    /**
     * 创建角色。角色编码已存在时不写入，并返回 400。
     */
    fun add(input: RoleCreateInputView, fetcher: Fetcher<Roles>?): Roles {
        val result = saveCommand(input) {
            setMode(SaveMode.INSERT_IF_ABSENT)
        }.execute(fetcher)
        if (result.totalAffectedRowCount == 0) {
            throw BusinessException("角色编码已存在").code(400)
        }
        return result.modifiedEntity
    }

    /**
     * 按主键更新角色。目标行不存在时返回 400。
     * 状态不在这个入参里，走 updateStatus。
     */
    fun update(input: RoleUpdateInputView, fetcher: Fetcher<Roles>?): Roles {
        val result = saveCommand(input) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute(fetcher)
        if (result.totalAffectedRowCount == 0) {
            throw BusinessException("要更新的数据不存在").code(400)
        }
        return result.modifiedEntity
    }

    /** 逻辑删除。 */
    fun delete(id: Long) {
        deleteById(id, DeleteMode.AUTO)
    }

    /** 改状态。0 正常，1 禁用。 */
    fun updateStatus(id: Long, status: Int) {
        executeUpdate {
            set(table.status, status)
            where(table.id eq id)
        }
    }

    /**
     * 按动态入参改角色权限。只提交了的字段才会写入。
     * 返回受影响的行数，含中间表。
     */
    fun updatePermission(input: RolePermissionUpdateInputView): Int =
        sql.save(input).totalAffectedRowCount

    /**
     * 某用户当前有效角色的编码。只含状态为 0 的角色。
     */
    fun listRoleCodesByUserId(userId: Long): List<String> =
        createQuery {
            where(table.users { id eq userId })
            where(table.status eq 0)
            select(table.roleCode)
        }.distinct().execute()

    /** 按 Specification 查列表，不分页。specification 为空时不加条件。 */
    fun queryList(specification: KSpecification<Roles>?, fetcher: Fetcher<Roles>?): List<Roles> =
        createQuery {
            where(specification)
            select(table.fetch(fetcher))
        }.execute()

    /** 自定义条件的列表查询，不分页。条件和返回列写在 block 里。 */
    fun <R> queryList(
        block: KMutableRootQuery.ForEntity<Roles>.() -> KConfigurableRootQuery<KNonNullTable<Roles>, R>,
    ): List<R> = createQuery(block).execute()

    /** 按 Specification 查一页。排序来自 Pageable，总数由 Jimmer 另查一次。 */
    fun queryPage(
        pageable: Pageable,
        specification: KSpecification<Roles>?,
        fetcher: Fetcher<Roles>?,
    ): Page<Roles> =
        createQuery {
            where(specification)
            orderBy(pageable.sort)
            select(table.fetch(fetcher))
        }.fetchSpringPage(pageable)

    /** 物理删除，给超级管理员清数据。普通删除用 delete。 */
    fun adminDelete(id: Long) {
        executeDelete {
            where(table.id eq id)
            setMode(DeleteMode.PHYSICAL)
        }
    }
}
