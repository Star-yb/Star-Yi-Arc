package com.star.common.jimmer

import org.babyfish.jimmer.sql.runtime.AbstractScalarProvider
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * 把实体里的 UUID 和数据库里的字符串互转。
 * 属性类型是 UUID、列类型是 varchar 或 text 时，Jimmer 读写都会走这里。
 */
@Component
class UuidScalarProvider : AbstractScalarProvider<UUID, String>() {

    /** 读库：字符串列还原成 UUID。 */
    override fun toScalar(sqlValue: String): UUID = UUID.fromString(sqlValue)

    /** 写库：UUID 存成标准字符串。 */
    override fun toSql(scalarValue: UUID): String = scalarValue.toString()
}
