package com.star.demo.entity

import com.star.model.entity.BaseEntity
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Table

/**
 * 演示公告，表 demo_notice。
 * 用来看通用 CRUD、详情重写，以及置顶查询、设置置顶这两条扩展接口。
 * 建表语句在 sql/sql.sql。
 */
@Entity
@Table(name = "demo_notice")
interface DemoNotice : BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    val title: String

    val content: String?

    /** 是否置顶。 */
    val pinned: Boolean
}
