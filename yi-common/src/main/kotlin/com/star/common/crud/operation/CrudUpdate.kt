package com.star.common.crud.operation

import cn.dev33.satoken.util.SaResult
import com.star.common.crud.annotation.CrudAction
import com.star.common.crud.annotation.CrudOperation
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

/** 更新。要换保存方式就覆盖 [update]，要换整段接口就覆盖 [updateById]。 */
interface CrudUpdate<E : Any, ID : Any, U : Any> {
    fun bindId(id: ID, input: U): U

    fun update(input: U): E

    @CrudOperation(CrudAction.UPDATE)
    @PutMapping("/{id}")
    fun updateById(@PathVariable id: ID, @RequestBody input: U): SaResult =
        SaResult.data(update(bindId(id, input)))
}
