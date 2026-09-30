package com.star.common.crud.web

import com.star.common.crud.annotation.CrudAction
import com.star.common.crud.annotation.CrudEndpoints
import com.star.common.crud.annotation.CrudOperation
import org.springframework.boot.webmvc.autoconfigure.WebMvcRegistrations
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.MergedAnnotations
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.lang.reflect.Method

/**
 * 五条基础接口是否注册，看 Controller 上的 [CrudEndpoints]。
 * 子类重写了某一条时，仍然认父方法上的标记，调用的是子类重写后的方法。
 * 没有 [CrudOperation] 的方法照常注册。
 */
class CrudEndpointHandlerMapping : RequestMappingHandlerMapping() {

    override fun getMappingForMethod(method: Method, handlerType: Class<*>): RequestMappingInfo? {
        val source = crudSource(method, handlerType)
        if (source == null) {
            return super.getMappingForMethod(method, handlerType)
        }
        val action = actionOf(source) ?: return null
        val enabled = enabledActions(handlerType)
        if (enabled == null || action !in enabled) {
            return null
        }
        return super.getMappingForMethod(source, handlerType)
    }

    private fun crudSource(method: Method, handlerType: Class<*>): Method? {
        if (actionOf(method) != null) {
            return method
        }
        return findAnnotated(handlerType, method.name, method.parameterTypes)
    }

    private fun findAnnotated(type: Class<*>?, name: String, parameterTypes: Array<Class<*>>): Method? {
        if (type == null || type == Any::class.java) {
            return null
        }
        type.declaredMethods.firstOrNull { candidate ->
            candidate.name == name &&
                candidate.parameterTypes.contentEquals(parameterTypes) &&
                actionOf(candidate) != null
        }?.let { return it }
        type.interfaces.firstNotNullOfOrNull { findAnnotated(it, name, parameterTypes) }?.let { return it }
        return findAnnotated(type.superclass, name, parameterTypes)
    }

    private fun actionOf(method: Method): CrudAction? =
        AnnotatedElementUtils.findMergedAnnotation(method, CrudOperation::class.java)?.value

    private fun enabledActions(handlerType: Class<*>): Set<CrudAction>? {
        val found = MergedAnnotations.from(handlerType, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
            .stream(CrudEndpoints::class.java)
            .map { it.synthesize() }
            .toList()
        if (found.isEmpty()) {
            return null
        }
        return found.flatMap { it.value.toList() }.toSet()
    }
}

@Component
class CrudEndpointConfiguration : WebMvcRegistrations {
    override fun getRequestMappingHandlerMapping(): RequestMappingHandlerMapping = CrudEndpointHandlerMapping()
}
