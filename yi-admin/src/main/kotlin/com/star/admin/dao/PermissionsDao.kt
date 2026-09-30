package com.star.admin.dao

import com.star.admin.dto.PermissionCreateInputView
import com.star.admin.dto.PermissionSpecification
import com.star.admin.dto.PermissionUpdateInputView
import com.star.common.exception.BusinessException
import com.star.identity.entity.Permissions
import com.star.identity.entity.id
import com.star.identity.entity.permissionCode
import com.star.identity.entity.roleCode
import com.star.identity.entity.roles
import com.star.identity.entity.status
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
 * 权限数据访问。查询条件写在 createQuery 里。
 */
@Repository
class PermissionsDao(
    sql: KSqlClient,
) : AbstractKotlinRepository<Permissions, Long>(sql) {

    /**
     * 创建权限。权限编码已存在时不写入，并返回 400。
     */
    fun add(input: PermissionCreateInputView, fetcher: Fetcher<Permissions>?): Permissions {
        val result = saveCommand(input) {
            setMode(SaveMode.INSERT_IF_ABSENT)
        }.execute(fetcher)
        if (result.totalAffectedRowCount == 0) {
            throw BusinessException("权限编码已存在").code(400)
        }
        return result.modifiedEntity
    }

    /**
     * 按主键更新权限。目标行不存在时返回 400。
     * 动态入参只改提交了的字段。状态走 updateStatus。
     */
    fun update(input: PermissionUpdateInputView, fetcher: Fetcher<Permissions>?): Permissions {
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
     * 某角色当前有效权限的编码。只含状态为 0 的权限。
     */
    fun listPermissionCodesByRoleCode(code: String): List<String> =
        createQuery {
            where(table.roles { roleCode eq code })
            where(table.status eq 0)
            select(table.permissionCode)
        }.distinct().execute()

    /** 按 Specification 查列表，不分页。specification 为空时不加条件。 */
    fun queryList(specification: KSpecification<Permissions>?, fetcher: Fetcher<Permissions>?): List<Permissions> =
        createQuery {
            where(specification)
            select(table.fetch(fetcher))
        }.execute()

    /** 自定义条件的列表查询，不分页。条件和返回列写在 block 里。 */
    fun <R> queryList(
        block: KMutableRootQuery.ForEntity<Permissions>.() -> KConfigurableRootQuery<KNonNullTable<Permissions>, R>,
    ): List<R> = createQuery(block).execute()

    /** 按 Specification 查一页。排序来自 Pageable，总数由 Jimmer 另查一次。 */
    fun queryPage(
        pageable: Pageable,
        specification: KSpecification<Permissions>?,
        fetcher: Fetcher<Permissions>?,
    ): Page<Permissions> =
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
