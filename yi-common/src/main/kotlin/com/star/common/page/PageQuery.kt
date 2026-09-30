package com.star.common.page

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

/**
 * 列表接口的分页入参。页码从 1 开始。没传或小于 1 时按第 1 页。
 * sort 可以是单个字段，也可以用逗号分隔多个字段；字段名后加 :desc 或 :asc 覆盖默认方向。
 * sort 为空时按 id 升序。登录日志会在调用前把空排序改成 loginTime 降序。
 */
class PageQuery {
    /** 页码，从 1 开始。没传或小于 1 时按第 1 页。 */
    var page: Int? = 1

    /** 每页条数，小于等于 0 时按 10 处理。 */
    var size: Int? = 10

    /**
     * 排序字段。多个字段用逗号分隔，例如 "createdTime:desc,id"。
     * 为空时按 id 排序。
     */
    var sort: String? = null

    /** 默认排序方向，ASC 或 DESC。单个字段写了 :desc 时以字段上的方向为准。 */
    var order: String? = "ASC"

    /** 转成 Spring Data 的 Pageable。页码减 1，因为 Spring Data 从 0 开始。 */
    fun toPageable(): Pageable {
        val safeSize = if (size == null || size!! <= 0) 10 else size!!
        val pageIndex = ((page ?: 1).coerceAtLeast(1)) - 1
        val direction = try {
            Sort.Direction.fromString(order ?: "ASC")
        } catch (_: Exception) {
            Sort.Direction.ASC
        }
        val sortText = sort
        if (sortText.isNullOrBlank()) {
            return PageRequest.of(pageIndex, safeSize, Sort.by(direction, "id"))
        }
        return PageRequest.of(pageIndex, safeSize, buildSort(sortText, direction))
    }

    private fun buildSort(sortText: String, defaultDirection: Sort.Direction): Sort {
        val orders = mutableListOf<Sort.Order>()
        for (raw in sortText.split(",")) {
            val token = raw.trim()
            if (token.isEmpty()) {
                continue
            }
            if (token.contains(":")) {
                val parts = token.split(":", limit = 2)
                val field = parts[0].trim()
                val directionText = parts[1].trim()
                if (field.isNotEmpty()) {
                    val direction = try {
                        Sort.Direction.fromString(directionText)
                    } catch (_: Exception) {
                        defaultDirection
                    }
                    orders.add(Sort.Order(direction, field))
                }
                continue
            }
            orders.add(Sort.Order(defaultDirection, token))
        }
        if (orders.isEmpty()) {
            return Sort.unsorted()
        }
        return Sort.by(orders)
    }
}
