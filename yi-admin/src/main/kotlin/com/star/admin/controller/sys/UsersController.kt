package com.star.admin.controller.sys

import cn.dev33.satoken.annotation.SaCheckRole
import cn.dev33.satoken.stp.StpUtil
import cn.dev33.satoken.util.SaResult
import com.star.admin.dto.UserCreateInputView
import com.star.admin.dto.UserRoleUpdateInputView
import com.star.admin.dto.UserSpecification
import com.star.admin.dto.UserUpdateInputView
import com.star.admin.service.UsersService
import com.star.common.page.PageQuery
import com.star.identity.entity.Users
import com.star.utils.AuthCryptoUtils
import com.star.utils.ExcelUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.format.DateTimeFormatter

/**
 * 用户接口，路径 /users。需要登录，重置密码和导出还要求角色 *。
 */
@Tag(name = "用户管理", description = "用户 CRUD、个人资料、角色绑定与导出，路径 /users")
@RestController
@RequestMapping("/users")
class UsersController(
    private val usersService: UsersService,
) {

    /**
     * 按条件查一页。page、size、sort、order 来自查询参数。
     */
    @Operation(summary = "分页查询用户列表")
    @GetMapping
    fun getUserList(pageQuery: PageQuery, specification: UserSpecification): SaResult =
        SaResult.data(usersService.listUsers(pageQuery, specification))

    @Operation(summary = "创建用户", description = "默认密码 123456（已哈希）")
    @PostMapping
    fun createUser(@RequestBody user: UserCreateInputView): SaResult {
        val created = user.copy(password = AuthCryptoUtils.hash("123456"))
        return SaResult.data(usersService.createUser(created))
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody user: UserUpdateInputView): SaResult =
        SaResult.data(usersService.updateUser(user.copy(id = id)))

    @Operation(summary = "删除用户", description = "逻辑删除")
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): SaResult {
        usersService.deleteUser(id)
        return SaResult.ok("删除成功")
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): SaResult =
        SaResult.data(usersService.obtainUser(id))

    @Operation(summary = "当前登录用户资料")
    @GetMapping("/myInfo")
    fun myInfo(): SaResult {
        StpUtil.checkLogin()
        return SaResult.data(usersService.obtainUser(StpUtil.getLoginIdAsLong()))
    }

    @Operation(summary = "修改当前用户密码")
    @PostMapping("/changePassword")
    fun changePassword(@RequestBody request: ChangePasswordRequest): SaResult {
        usersService.changePassword(StpUtil.getLoginIdAsLong(), request.oldPassword, request.newPassword)
        return SaResult.ok("修改成功")
    }

    @Operation(summary = "启用用户")
    @PostMapping("/{id}/enable")
    fun enableStatus(@PathVariable id: Long): SaResult {
        usersService.enableUser(id)
        return SaResult.ok("修改成功")
    }

    @Operation(summary = "禁用用户", description = "状态改为禁用，并注销该账号全部已登录会话")
    @PostMapping("/{id}/disable")
    fun disableStatus(@PathVariable id: Long): SaResult {
        usersService.disableUser(id)
        return SaResult.ok("修改成功")
    }

    @Operation(summary = "查询用户已绑定角色")
    @GetMapping("/roles/{id}")
    fun getRoleUsers(@PathVariable id: Long): SaResult =
        SaResult.data(usersService.getRoleUsers(id))

    @Operation(summary = "更新用户角色分配")
    @PutMapping("/{id}/roles")
    fun updateRole(@PathVariable id: Long, @RequestBody input: UserRoleUpdateInputView): SaResult =
        SaResult.data(usersService.updateRole(input))

    @Operation(summary = "重置用户密码", description = "需超级管理员角色")
    @SaCheckRole("*")
    @PostMapping("/{id}/resetPassword")
    fun resetPassword(@PathVariable id: Long): SaResult {
        usersService.resetPassword(id)
        return SaResult.ok("修改成功")
    }

    @Operation(summary = "导出用户 Excel", description = "需超级管理员角色；直接下载文件流")
    @SaCheckRole("*")
    @GetMapping("/export")
    fun exportUsers(response: HttpServletResponse) {
        val users = usersService.listUsers(null)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val columns = listOf(
            ExcelUtils.Column<Users>("用户ID") { it.id },
            ExcelUtils.Column("用户名") { it.username },
            ExcelUtils.Column("昵称") { it.nickname ?: "" },
            ExcelUtils.Column("邮箱") { it.email ?: "" },
            ExcelUtils.Column("手机号") { it.phone ?: "" },
            ExcelUtils.Column("状态") { toStatusText(it.status) },
            ExcelUtils.Column("是否超管") { if (it.superAdmin == 1) "是" else "否" },
            ExcelUtils.Column("最后登录时间") { it.lastLoginTime?.format(formatter) ?: "" },
        )
        ExcelUtils.writeByColumnsResponse(response, "users-export", "用户列表", columns, users)
    }

    private fun toStatusText(status: Int): String = when (status) {
        0 -> "正常"
        1 -> "禁用"
        2 -> "删除"
        else -> status.toString()
    }
}

/** 修改当前用户密码。用户从登录态取，请求体只带原密码和新密码。 */
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String,
)
