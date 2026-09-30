package com.star.demo.controller

import cn.dev33.satoken.util.SaResult
import com.star.common.crud.annotation.CrudAll
import com.star.common.crud.JimmerCrudResource
import com.star.common.exception.BusinessException
import com.star.demo.dto.DemoNoticeCreateInput
import com.star.demo.dto.DemoNoticeSpecification
import com.star.demo.dto.DemoNoticeUpdateInput
import com.star.demo.dto.DemoNoticeView
import com.star.demo.entity.DemoNotice
import com.star.demo.entity.id
import com.star.demo.entity.pinned
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 演示公告。五条基础接口由 [CrudAll] 开放。
 * 详情覆盖了 [obtainById]，其余四条用默认实现。
 * 置顶列表和设置置顶是额外接口，不经过五条基础动作。
 */
@Tag(name = "演示-公告", description = "DemoNotice 标准 CRUD 与两条扩展接口，路径 /demo/notices")
@RestController
@RequestMapping("/demo/notices")
@CrudAll
class DemoNoticeController(
    sql: KSqlClient,
) : JimmerCrudResource<
    DemoNotice,
    Long,
    DemoNoticeCreateInput,
    DemoNoticeUpdateInput,
    DemoNoticeSpecification,
    >(
    sql,
    DemoNotice::class,
    DemoNoticeView.METADATA.fetcher,
) {

    override fun bindId(id: Long, input: DemoNoticeUpdateInput): DemoNoticeUpdateInput =
        input.copy(id = id)

    override fun obtainById(@PathVariable("id") id: Long): SaResult {
        println("查询详情方法被重构了")
        return super.obtainById(id)
    }

    /** 当前置顶的公告。 */
    @Operation(summary = "查询置顶公告")
    @GetMapping("/pinned")
    fun listPinned(): SaResult {
        val notices = sql.createQuery(DemoNotice::class) {
            where(table.pinned eq true)
            select(table.fetch(fetcher))
        }.execute()
        return SaResult.data(notices)
    }

    /** 只改置顶标记，不走通用更新。 */
    @Operation(summary = "设置是否置顶")
    @PutMapping("/{id}/pin")
    fun pin(@PathVariable id: Long, @RequestBody request: DemoNoticePinRequest): SaResult {
        val affected = sql.createUpdate(DemoNotice::class) {
            set(table.pinned, request.pinned)
            where(table.id eq id)
        }.execute()
        if (affected == 0) {
            throw BusinessException("数据不存在").code(404)
        }
        return SaResult.ok("设置成功")
    }
}

/** 设置置顶的请求体。 */
data class DemoNoticePinRequest(
    val pinned: Boolean,
)
