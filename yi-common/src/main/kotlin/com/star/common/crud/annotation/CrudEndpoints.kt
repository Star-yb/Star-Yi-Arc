package com.star.common.crud.annotation

/**
 * 标在 Controller 上，决定五条基础接口里开放哪几条。
 * 没写进注解的不会注册。Controller 里另外写的方法不受影响。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CrudEndpoints(
    vararg val value: CrudAction,
)

/** 只有分页列表和按主键查询。 */
@CrudEndpoints(CrudAction.LIST, CrudAction.OBTAIN)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CrudReadable

/** 只有创建、更新、删除。 */
@CrudEndpoints(CrudAction.CREATE, CrudAction.UPDATE, CrudAction.DELETE)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CrudWritable

/** 列表、详情、创建、更新、删除都有。 */
@CrudEndpoints(
    CrudAction.LIST,
    CrudAction.OBTAIN,
    CrudAction.CREATE,
    CrudAction.UPDATE,
    CrudAction.DELETE,
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CrudAll
