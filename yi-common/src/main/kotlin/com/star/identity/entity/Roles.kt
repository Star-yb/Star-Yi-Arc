package com.star.identity.entity

import com.star.model.entity.BaseEntity
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToMany
import org.babyfish.jimmer.sql.Table

/**
 * 角色，表 sys_roles，主键自增。
 * 和用户、权限都是多对多，关联的维护端在 Users 和 Permissions 上。
 */
@Entity
@Table(name = "sys_roles")
interface Roles : BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @Key
    val roleCode: String

    val roleName: String

    val description: String?

    /** 0 正常，1 禁用，2 删除 */
    val status: Int

    @ManyToMany(mappedBy = "roles")
    val users: List<Users>

    @ManyToMany(mappedBy = "roles")
    val permissions: List<Permissions>
}
