package com.star.utils

/**
 * HTTP 调用的结果。statusCode 是 HTTP 状态码，body 是响应原文。
 */
data class HttpResult(
    val statusCode: Int,
    val body: String,
) {
    /** HTTP 2xx 视为成功。 */
    fun isSuccessful(): Boolean = statusCode in 200..299
}
