package com.star.common.crud.annotation

/** 五条基础接口。注解里写出的才会注册成路由。 */
enum class CrudAction {
    /** 分页列表。 */
    LIST,

    /** 按主键查询。 */
    OBTAIN,

    /** 创建。 */
    CREATE,

    /** 更新。 */
    UPDATE,

    /** 删除。 */
    DELETE,
}
