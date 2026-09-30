package com.star.common.crud.operation

import cn.dev33.satoken.util.SaResult
import com.star.common.crud.annotation.CrudAction
import com.star.common.crud.annotation.CrudOperation
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable

/** 删除。要换删除方式就覆盖 [delete]，要换整段接口就覆盖 [deleteById]。 */
interface CrudDelete<ID : Any> {
    fun delete(id: ID)

    @CrudOperation(CrudAction.DELETE)
    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: ID): SaResult {
        delete(id)
        return SaResult.ok("删除成功")
    }
}
