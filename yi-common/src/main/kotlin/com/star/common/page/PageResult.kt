package com.star.common.page

import org.springframework.data.domain.Page

/**
 * 列表接口的统一返回。分页和不分页都用这个结构，前端看 paged 判断。
 * page 从 1 开始，和 PageQuery 一致；Spring Data 内部页码从 0 开始，ofPaged 里会加 1。
 */
class PageResult<T : Any> {
    var paged: Boolean = false
    var page: Int = 0
    var size: Int = 0
    var first: Boolean = false
    var last: Boolean = false
    var count: Long = 0
    var results: List<T>? = null

    val totalPages: Int
        get() {
            if (count <= 0) {
                return 0
            }
            if (!paged || size <= 0) {
                return 1
            }
            return ((count + size - 1) / size).toInt()
        }

    companion object {
        /**
         * 整表结果放进 results。列表接口不走这里。
         * 有数据时 page 为 1，避免模板用 (page - 1) * size 算出负数。
         */
        fun <T : Any> ofUnpaged(records: List<T>?): PageResult<T> {
            val total = records?.size ?: 0
            val result = PageResult<T>()
            result.paged = false
            result.page = if (total == 0) 0 else 1
            result.size = total
            result.first = true
            result.last = true
            result.count = total.toLong()
            result.results = records
            return result
        }

        /** 分页：把 Spring Data 的 Page 转成从 1 开始的页码。 */
        fun <T : Any> ofPaged(pageData: Page<T>): PageResult<T> {
            val result = PageResult<T>()
            result.paged = true
            result.page = pageData.number + 1
            result.size = pageData.size
            result.first = pageData.isFirst
            result.last = pageData.isLast
            result.count = pageData.totalElements
            result.results = pageData.content
            return result
        }
    }
}
