package com.star.utils

import cn.dev33.satoken.secure.SaSecureUtil

/**
 * 密码摘要。入库前用 hash，登录时用 matches 比对明文和库里的摘要。
 * 使用 Sa-Token 的 SHA-256，没有加盐。
 */
object AuthCryptoUtils {

    /** 明文转 SHA-256 十六进制字符串。 */
    fun hash(plainText: String): String = SaSecureUtil.sha256(plainText)

    /** 明文和库里的摘要是否一致。任一方为 null 都视为不匹配。 */
    fun matches(plainText: String?, hashedText: String?): Boolean =
        plainText != null && hashedText != null && hash(plainText) == hashedText
}
