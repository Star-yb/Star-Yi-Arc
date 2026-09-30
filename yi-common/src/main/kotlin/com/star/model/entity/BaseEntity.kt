package com.star.model.entity

import com.fasterxml.jackson.annotation.JsonFormat
import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.LogicalDeleted
import org.babyfish.jimmer.sql.MappedSuperclass
import java.time.LocalDateTime

/**
 * 业务表共用的审计字段：创建时间、修改时间、删除时间、逻辑删除标记。
 * deleted 为 true 时 Jimmer 按逻辑删除处理，普通查询不会查出这些行。
 */
@MappedSuperclass
interface BaseEntity {

    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdTime: LocalDateTime

    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val modifiedTime: LocalDateTime

    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val deletedTime: LocalDateTime?

    @LogicalDeleted("true")
    @Column(name = "is_deleted")
    val deleted: Boolean
}
