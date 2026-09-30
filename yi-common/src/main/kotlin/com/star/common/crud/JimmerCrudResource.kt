package com.star.common.crud

import com.star.common.crud.operation.CrudCreate
import com.star.common.crud.operation.CrudDelete
import com.star.common.crud.operation.CrudList
import com.star.common.crud.operation.CrudObtain
import com.star.common.crud.operation.CrudUpdate
import com.star.common.exception.BusinessException
import com.star.common.page.PageQuery
import com.star.common.page.PageResult
import org.babyfish.jimmer.Input
import org.babyfish.jimmer.spring.repository.fetchSpringPage
import org.babyfish.jimmer.spring.repository.orderBy
import org.babyfish.jimmer.sql.ast.mutation.DeleteMode
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.fetcher.Fetcher
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.query.specification.KSpecification
import kotlin.reflect.KClass

/**
 * 五条基础接口的默认实现。
 * 子类加上 [com.star.common.crud.annotation.CrudEndpoints]、[com.star.common.crud.annotation.CrudReadable]、[com.star.common.crud.annotation.CrudWritable] 或 [com.star.common.crud.annotation.CrudAll]，才决定开放哪几条。
 * 要改某一条，在 Controller 里覆盖对应方法，里面可以调用自己的 Service。
 */
abstract class JimmerCrudResource<
    E : Any,
    ID : Any,
    C : Input<E>,
    U : Input<E>,
    S : KSpecification<E>,
    >(
    protected val sql: KSqlClient,
    protected val entityType: KClass<E>,
    protected val fetcher: Fetcher<E>?,
) : CrudList<E, S>,
    CrudObtain<E, ID>,
    CrudCreate<E, C>,
    CrudUpdate<E, ID, U>,
    CrudDelete<ID> {

    protected open fun beforeCreate(input: C) {}

    protected open fun afterCreate(entity: E) {}

    protected open fun beforeUpdate(input: U) {}

    protected open fun afterUpdate(entity: E) {}

    protected open fun beforeDelete(id: ID) {}

    protected open fun afterDelete(id: ID) {}

    /** 把路径上的 id 放进更新对象。子类用 copy 返回新对象。 */
    override fun bindId(id: ID, input: U): U = input

    override fun create(input: C): E {
        beforeCreate(input)
        val result = sql.saveCommand(input) {
            setMode(SaveMode.INSERT_ONLY)
        }.execute(fetcher)
        if (result.totalAffectedRowCount == 0) {
            throw BusinessException("新增失败，未写入数据").code(400)
        }
        val entity = result.modifiedEntity
        afterCreate(entity)
        return entity
    }

    override fun update(input: U): E {
        beforeUpdate(input)
        val result = sql.saveCommand(input) {
            setMode(SaveMode.UPDATE_ONLY)
        }.execute(fetcher)
        if (result.totalAffectedRowCount == 0) {
            throw BusinessException("要更新的数据不存在").code(400)
        }
        val entity = result.modifiedEntity
        afterUpdate(entity)
        return entity
    }

    override fun delete(id: ID) {
        beforeDelete(id)
        sql.deleteById(entityType, id, DeleteMode.AUTO)
        afterDelete(id)
    }

    override fun obtain(id: ID): E {
        val entity = if (fetcher == null) {
            sql.findById(entityType, id)
        } else {
            sql.findById(fetcher, id)
        }
        return entity ?: throw BusinessException("数据不存在").code(404)
    }

    override fun page(pageQuery: PageQuery, specification: S?): PageResult<E> {
        val pageable = pageQuery.toPageable()
        val page = sql.createQuery(entityType) {
            where(specification)
            orderBy(pageable.sort)
            select(table.fetch(fetcher))
        }.fetchSpringPage(pageable)
        return PageResult.Companion.ofPaged(page)
    }
}