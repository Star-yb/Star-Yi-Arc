package com.star.openapi

/**
 * OpenAPI 文档里共用的常量。
 * 接口上的 @SecurityRequirement 引用 SECURITY_SCHEME_SATOKEN，和 Sa-Token 的 token 名对齐。
 */
object ApiDocConstants {
    /** springdoc 安全方案名，对应请求头里的 satoken。 */
    const val SECURITY_SCHEME_SATOKEN: String = "satoken"
}
