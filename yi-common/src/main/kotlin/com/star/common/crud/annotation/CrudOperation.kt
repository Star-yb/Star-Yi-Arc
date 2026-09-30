package com.star.common.crud.annotation

/** 标在五条基础方法上，供注册时识别。业务方法不要加。 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CrudOperation(
    val value: CrudAction,
)
