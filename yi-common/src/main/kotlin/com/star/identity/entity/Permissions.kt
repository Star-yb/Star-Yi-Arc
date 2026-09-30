package com.star.identity.entity

import com.star.model.entity.BaseEntity
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.ForeignKeyType
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.GenerationType
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.JoinTable
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToMany
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.OneToMany
import org.babyfish.jimmer.sql.Table

/**
 * 权限，表 sys_permissions，主键自增。
 * permissionType 区分菜单、按钮、接口；parent 组成树。
 * 和角色的多对多走中间表 sys_role_permission。
 */
@Entity
@Table(name = "sys_permissions")
interface Permissions : BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long

    @Key
    val permissionCode: String

    val permissionName: String

    /** 1 菜单，2 按钮，3 接口 */
    val permissionType: Int

    @ManyToOne
    @JoinColumn(name = "parent_id", foreignKeyType = ForeignKeyType.FAKE)
    val parent: Permissions?

    @OneToMany(mappedBy = "parent")
    val childPermissions: List<Permissions>

    val path: String?

    val icon: String?

    val sortOrder: Int

    /** 0 正常，1 禁用，2 删除 */
    val status: Int

    @ManyToMany
    @JoinTable(
        name = "sys_role_permission",
        joinColumnName = "permission_id",
        inverseJoinColumnName = "role_id",
    )
    val roles: List<Roles>

    @IdView("parent")
    val parentId: Long?
}
