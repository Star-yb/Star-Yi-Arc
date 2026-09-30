package com.star.common.exception

/**
 * 业务上可预期的失败，例如参数不合法、状态不允许操作。
 * 默认业务码 500，调用 code() 可以改成 400 等。
 * [com.star.common.globalexception.GlobalExceptionHandler] 会按这个码返回 SaResult。
 */
class BusinessException : RuntimeException {

    /** 返回给前端的业务码，默认 500。 */
    var code: Int = 500
        private set

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)

    /** 指定返回给前端的业务码，可链式调用。 */
    fun code(code: Int): BusinessException {
        this.code = code
        return this
    }
}
