package com.star.identity.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.star.common.jimmer.VerifiableOrderedIdGenerator
import com.star.model.entity.BaseEntity
import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.IdView
import org.babyfish.jimmer.sql.JoinTable
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.ManyToMany
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime

/**
 * 后台用户，表 sys_users。
 * 主键不用数据库自增，由 VerifiableOrderedIdGenerator 生成可校验的有序 Long。
 * 角色通过中间表 sys_user_role 关联。
 */
@Entity
@Table(name = "sys_users")
interface Users : BaseEntity {

    @Id
    @GeneratedValue(generatorType = VerifiableOrderedIdGenerator::class)
    val id: Long

    @Key
    val username: String

    val password: String

    val nickname: String?

    val email: String?

    val phone: String?

    /** 0 正常，1 禁用，2 删除 */
    val status: Int

    /** 0 否，1 是 */
    @Column(name = "is_super_admin")
    val superAdmin: Int

    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val lastLoginTime: LocalDateTime?

    @ManyToMany
    @JoinTable(
        name = "sys_user_role",
        joinColumnName = "user_id",
        inverseJoinColumnName = "role_id",
    )
    val roles: List<Roles>

    @IdView("roles")
    val roleIds: List<Long>
}
