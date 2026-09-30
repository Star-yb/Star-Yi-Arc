package com.star.common.crud.operation

import cn.dev33.satoken.util.SaResult
import com.star.common.crud.annotation.CrudAction
import com.star.common.crud.annotation.CrudOperation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

/** 按主键查询。要换读取方式就覆盖 [obtain]，要换整段接口就覆盖 [obtainById]。 */
interface CrudObtain<E : Any, ID : Any> {
    fun obtain(id: ID): E

    @CrudOperation(CrudAction.OBTAIN)
    @GetMapping("/{id}")
    fun obtainById(@PathVariable id: ID): SaResult = SaResult.data(obtain(id))
}
