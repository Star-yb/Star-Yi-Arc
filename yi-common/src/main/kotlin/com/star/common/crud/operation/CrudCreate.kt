package com.star.common.crud.operation

import cn.dev33.satoken.util.SaResult
import com.star.common.crud.annotation.CrudAction
import com.star.common.crud.annotation.CrudOperation
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

/** 创建。要换保存方式就覆盖 [create]，要换整段接口就覆盖 [createOne]。 */
interface CrudCreate<E : Any, C : Any> {
    fun create(input: C): E

    @CrudOperation(CrudAction.CREATE)
    @PostMapping
    fun createOne(@RequestBody input: C): SaResult = SaResult.data(create(input))
}
