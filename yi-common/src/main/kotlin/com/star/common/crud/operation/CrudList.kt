package com.star.common.crud.operation

import cn.dev33.satoken.util.SaResult
import com.star.common.crud.annotation.CrudAction
import com.star.common.crud.annotation.CrudOperation
import com.star.common.page.PageQuery
import com.star.common.page.PageResult
import org.springframework.web.bind.annotation.GetMapping

/** 分页列表。要换查询就覆盖 [page]，要换整段接口就覆盖 [list]。 */
interface CrudList<E : Any, S : Any> {
    fun page(pageQuery: PageQuery, specification: S?): PageResult<E>

    @CrudOperation(CrudAction.LIST)
    @GetMapping
    fun list(pageQuery: PageQuery, specification: S?): SaResult =
        SaResult.data(page(pageQuery, specification))
}
