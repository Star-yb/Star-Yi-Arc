package com.star.admin.controller.sys

import cn.dev33.satoken.util.SaResult
import com.star.admin.dto.PermissionCreateInputView
import com.star.admin.dto.PermissionSpecification
import com.star.admin.dto.PermissionUpdateInputView
import com.star.admin.service.PermissionsService
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
 * 权限接口，路径 /permissions。需要登录。
 */
@Tag(name = "权限管理", description = "权限 CRUD、启停与树形查询，路径 /permissions")
@RestController
@RequestMapping("/permissions")
class PermissionsController(
    private val permissionsService: PermissionsService,
) {

    @Operation(summary = "分页查询权限列表")
    @GetMapping
    fun listPermissions(pageQuery: PageQuery, specification: PermissionSpecification): SaResult =
        SaResult.data(permissionsService.listPermissions(pageQuery, specification))

    @Operation(summary = "创建权限")
    @PostMapping
    fun createPermission(@RequestBody permission: PermissionCreateInputView): SaResult =
        SaResult.data(permissionsService.createPermission(permission))

    @Operation(summary = "更新权限")
    @PutMapping("/{id}")
    fun updatePermission(
        @PathVariable id: Long,
        @RequestBody permission: PermissionUpdateInputView,
    ): SaResult = SaResult.data(permissionsService.updatePermission(permission.copy(id = id)))

    @Operation(summary = "删除权限", description = "逻辑删除")
    @DeleteMapping("/{id}")
    fun deletePermission(@PathVariable id: Long): SaResult {
        permissionsService.deletePermission(id)
        return SaResult.ok("删除成功")
    }

    @Operation(summary = "权限详情")
    @GetMapping("/{id}")
    fun getPermissionById(@PathVariable id: Long): SaResult =
        SaResult.data(permissionsService.obtainPermission(id))

    @Operation(summary = "启用权限")
    @PostMapping("/{id}/enable")
    fun enablePermission(@PathVariable id: Long): SaResult {
        permissionsService.enablePermission(id)
        return SaResult.ok("启用成功")
    }

    @Operation(summary = "禁用权限")
    @PostMapping("/{id}/disable")
    fun disablePermission(@PathVariable id: Long): SaResult {
        permissionsService.disablePermission(id)
        return SaResult.ok("禁用成功")
    }

    @Operation(summary = "查询角色已绑定权限")
    @GetMapping("/role/{roleId}")
    fun getPermissionsByRole(@PathVariable roleId: Long): SaResult =
        SaResult.data(permissionsService.getPermissionRoles(roleId))

    @Operation(summary = "查询子权限列表", description = "按父级 ID 查询直接子节点")
    @GetMapping("/parent/{parentId}")
    fun getPermissionsByParent(@PathVariable parentId: Long): SaResult =
        SaResult.data(permissionsService.getChildPermissions(parentId))

    @Operation(summary = "权限树查询")
    @GetMapping("/tree")
    fun getPermissionsTree(pageQuery: PageQuery, specification: PermissionSpecification): SaResult =
        SaResult.data(permissionsService.getPermissionsTree(pageQuery, specification))
}
