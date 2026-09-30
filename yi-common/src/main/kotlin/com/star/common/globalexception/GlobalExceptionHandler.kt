package com.star.common.globalexception

import cn.dev33.satoken.exception.NotLoginException
import cn.dev33.satoken.exception.SaTokenException
import cn.dev33.satoken.util.SaResult
import com.star.common.exception.BusinessException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException

/**
 * 全局异常出口。Controller 抛出的异常在这里收成 SaResult，避免把堆栈直接返回给前端。
 *
 * Sa-Token 的常见码单独翻译成中文：未登录、token 无效或过期、角色无权限。
 * BusinessException 使用异常上自带的业务码。其余未分类异常走最后的 Exception 处理。
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /** 30001 重定向地址无效；11011/11012/11013 登录态问题；11041 角色无权限。 */
    @ExceptionHandler(SaTokenException::class)
    fun handlerSaTokenException(
        exception: SaTokenException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): SaResult {
        if (exception.code == 30001) {
            return SaResult.error("redirect 重定向 url 是一个无效地址")
        }
        if (exception.code == 11011) {
            return SaResult.notLogin().setMsg("未能读取到有效 token")
        }
        if (exception.code == 11012) {
            return SaResult.notLogin().setMsg("提供的token 无效")
        }
        if (exception.code == 11013) {
            return SaResult.notLogin().setMsg("提供的token 已过期")
        }
        if (exception.code == 11041) {
            return SaResult.notLogin().setMsg("角色暂无权限")
        }
        exception.printStackTrace()
        return SaResult.error(exception.message).setCode(exception.code)
    }

    /** 未登录。NotLoginException 是 SaTokenException 的子类，更具体的文案已在上面处理。 */
    @ExceptionHandler(NotLoginException::class)
    fun handlerException(exception: NotLoginException): SaResult = SaResult.error(exception.message)

    /** 请求方法不对，例如用 GET 调了只接受 POST 的接口。 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handlerHttpRequestMethodNotSupportedException(
        exception: HttpRequestMethodNotSupportedException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): SaResult = SaResult.error("请求方法不支持").setCode(405)

    /** 没有匹配的接口。 */
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handlerNoHandlerFoundException(
        exception: NoHandlerFoundException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): SaResult = SaResult.error("接口不存在").setCode(404)

    /** 静态资源不存在。 */
    @ExceptionHandler(NoResourceFoundException::class)
    fun handlerNoResourceFoundException(
        exception: NoResourceFoundException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): SaResult = SaResult.error("资源不存在").setCode(404)

    /** 请求体缺失或 JSON 无法解析。 */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handlerHttpMessageNotReadableException(
        exception: HttpMessageNotReadableException,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): SaResult {
        log.error(exception.mostSpecificCause.toString())
        log.error(exception.cause.toString())
        return SaResult.error("缺少请求体或数据不完整").setCode(400)
    }

    /** 业务异常，状态码用异常上的 code。 */
    @ExceptionHandler(BusinessException::class)
    fun handlerBusinessException(exception: BusinessException): SaResult {
        return SaResult.error(exception.message).setCode(exception.code)
    }

    /** 未单独分类的异常。先打印堆栈，再把 message 返回给前端。 */
    @ExceptionHandler(Exception::class)
    fun handlerException(
        exception: Exception,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): SaResult {
        exception.printStackTrace()
        return SaResult.error(exception.message)
    }
}
