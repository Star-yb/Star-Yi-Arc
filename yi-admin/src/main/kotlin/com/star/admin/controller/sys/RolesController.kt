package com.star.admin.controller.sys

import cn.dev33.satoken.util.SaResult
import com.star.admin.dto.RoleCreateInputView
import com.star.admin.dto.RolePermissionUpdateInputView
import com.star.admin.dto.RoleSpecification
import com.star.admin.dto.RoleUpdateInputView
import com.star.admin.service.RolesService
import com.star.common.page.PageQuery
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 角色接口，路径 /roles。需要登录。
 */
@Tag(name = "角色管理", description = "角色 CRUD、启停与权限绑定，路径 /roles")
@RestController
@RequestMapping("/roles")
class RolesController(
    private val rolesService: RolesService,
) {

    @Operation(summary = "分页查询角色列表")
    @GetMapping
    fun listRoles(pageQuery: PageQuery, specification: RoleSpecification): SaResult =
        SaResult.data(rolesService.listRoles(pageQuery, specification))

    @Operation(summary = "创建角色")
    @PostMapping
    fun createRole(@RequestBody role: RoleCreateInputView): SaResult =
        SaResult.data(rolesService.createRole(role))

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    fun updateRole(@PathVariable id: Long, @RequestBody role: RoleUpdateInputView): SaResult =
        SaResult.data(rolesService.updateRole(role.copy(id = id)))

    @Operation(summary = "删除角色", description = "逻辑删除")
    @DeleteMapping("/{id}")
    fun deleteRole(@PathVariable id: Long): SaResult {
        rolesService.deleteRole(id)
        return SaResult.ok("删除成功")
    }

    @Operation(summary = "角色详情")
    @GetMapping("/{id}")
    fun getRoleById(@PathVariable id: Long): SaResult =
        SaResult.data(rolesService.obtainRole(id))

    @Operation(summary = "启用角色")
    @PostMapping("/{id}/enable")
    fun enableRole(@PathVariable id: Long): SaResult {
        rolesService.enableRole(id)
        return SaResult.ok("启用成功")
    }

    @Operation(summary = "禁用角色")
    @PostMapping("/{id}/disable")
    fun disableRole(@PathVariable id: Long): SaResult {
        rolesService.disableRole(id)
        return SaResult.ok("禁用成功")
    }

    /**
     * 路径上的 id 不参与更新，角色 id 以请求体为准。与 Java 版一致。
     */
    @Operation(summary = "更新角色权限绑定")
    @PutMapping("/{id}/permission")
    @Suppress("UNUSED_PARAMETER")
    fun updatePermission(
        @PathVariable id: Long,
        @RequestBody input: RolePermissionUpdateInputView,
    ): SaResult = SaResult.data(rolesService.updatePermission(input))

    @Operation(summary = "查询用户拥有的角色")
    @GetMapping("/user/{userId}")
    fun getUserRoles(@PathVariable userId: Long): SaResult =
        SaResult.data(rolesService.getUserRoles(userId))
}
