package com.star.admin.controller.webpage

import cn.dev33.satoken.annotation.SaIgnore
import cn.dev33.satoken.stp.StpUtil
import cn.dev33.satoken.util.SaResult
import com.star.admin.dto.PermissionSpecification
import com.star.admin.dto.RoleSpecification
import com.star.admin.dto.UserSpecification
import com.star.admin.service.AuthService
import com.star.admin.service.PermissionsService
import com.star.admin.service.RolesService
import com.star.admin.service.UsersService
import com.star.admin.support.LoginLogRecorder
import com.star.admin.support.LoginLogSupport
import com.star.admin.support.SaAuthLoginSupport
import com.star.common.exception.BusinessException
import com.star.common.page.PageQuery
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.mvc.support.RedirectAttributes

/**
 * 超级管理员后台页面，路径 /admin。
 * 用户、角色、权限和登录日志按页加载。下拉框和权限勾选仍取全量。
 */
@Tag(name = "超级管理员-后台页面", description = "Thymeleaf 管理后台页面与维护接口，路径 /admin")
@Controller
@RequestMapping("/admin")
class WebController(
    private val authService: AuthService,
    private val usersService: UsersService,
    private val rolesService: RolesService,
    private val permissionsService: PermissionsService,
    private val loginLogSupport: LoginLogSupport,
    private val loginLogRecorder: LoginLogRecorder,
    @param:Value("\${springdoc.swagger-ui.path:/swagger-ui.html}")
    private val swaggerUiPath: String,
) {

    @Operation(summary = "登录页", description = "返回 Thymeleaf 登录页面，免鉴权")
    @SaIgnore
    @GetMapping("/login")
    fun login(): String = "login"

    @Operation(summary = "表单登录", description = "Thymeleaf 表单提交登录，成功后重定向到用户管理页")
    @SaIgnore
    @PostMapping("/login")
    fun login(
        @RequestParam("username") username: String,
        @RequestParam("password") password: String,
        request: HttpServletRequest,
        redirectAttributes: RedirectAttributes,
    ): String {
        try {
            val user = authService.login(username, password)
            SaAuthLoginSupport.establishAdminWebSession(user.id)
            authService.onLoginSuccess(user.id)
            StpUtil.getSession().set("name", user.username)
            loginLogRecorder.recordLoginSuccess(request, user.id, username, StpUtil.getTokenValue())
            return "redirect:/admin/user"
        } catch (exception: BusinessException) {
            log.warn("后台登录失败: username={}, reason={}", username, exception.message)
            val userId = authService.resolveUserIdByLoginAccount(username)
            loginLogRecorder.recordLoginFailure(request, username, userId, exception.message ?: "登录失败")
            redirectAttributes.addFlashAttribute("errorMsg", exception.message)
            redirectAttributes.addFlashAttribute("historyUsername", username)
            return "redirect:/admin/login"
        }
    }

    @Operation(summary = "用户管理页", description = "返回用户列表 Thymeleaf 页面")
    @GetMapping("/user")
    fun user(pageQuery: PageQuery, specification: UserSpecification, model: Model): String {
        model.addAttribute("usersPageResult", usersService.listUsers(pageQuery, specification))
        model.addAttribute("rolesList", rolesService.listRoles(null))
        return "sys/user"
    }

    @Operation(summary = "退出登录", description = "仅注销 Thymeleaf 超管 Cookie 会话，不影响 API 请求头 token")
    @SaIgnore
    @GetMapping("/logout")
    fun logout(request: HttpServletRequest): String {
        if (StpUtil.isLogin()) {
            val userId = StpUtil.getLoginIdAsLong()
            val username = authService.resolveLoginUsername(userId)
            loginLogRecorder.recordLogout(request, userId, username, StpUtil.getTokenValue())
            StpUtil.logout(userId, SaAuthLoginSupport.DEVICE_ADMIN_WEB)
        }
        return "redirect:/admin/login?logout=true"
    }

    @Operation(summary = "角色管理页")
    @GetMapping("/role")
    fun role(pageQuery: PageQuery, specification: RoleSpecification, model: Model): String {
        model.addAttribute("rolesPageResult", rolesService.listRoles(pageQuery, specification))
        return "sys/role"
    }

    @Operation(summary = "角色权限配置页", description = "按 roleId 展示角色与权限勾选界面")
    @GetMapping("/role-permission")
    fun rolePermission(@RequestParam("roleId") roleId: Long, model: Model): String {
        model.addAttribute("role", rolesService.obtainRole(roleId))
        model.addAttribute("permissionsList", permissionsService.listPermissions(null))
        model.addAttribute("permissionRoles", permissionsService.getPermissionRoles(roleId))
        return "sys/role-permission"
    }

    @Operation(summary = "权限树管理页")
    @GetMapping("/permission")
    fun permission(pageQuery: PageQuery, specification: PermissionSpecification, model: Model): String {
        model.addAttribute("permissionsPageResult", permissionsService.getPermissionsTree(pageQuery, specification))
        return "sys/permission"
    }

    @Operation(summary = "权限列表页", description = "平铺列表视图")
    @GetMapping("/permission-list")
    fun permissionList(pageQuery: PageQuery, specification: PermissionSpecification, model: Model): String {
        model.addAttribute("permissionsPageResult", permissionsService.listPermissions(pageQuery, specification))
        return "sys/permission-list"
    }

    @Operation(summary = "接口文档页", description = "iframe 内嵌 Swagger UI，工具栏可新标签页打开")
    @GetMapping("/api-docs")
    fun apiDocs(model: Model): String {
        model.addAttribute("swaggerUiPath", swaggerUiPath)
        return "sys/api-docs"
    }

    @Operation(summary = "登录日志页", description = "支持按用户名、登录类型、状态筛选")
    @GetMapping("/login-log")
    fun loginLog(
        pageQuery: PageQuery,
        @RequestParam(required = false) username: String?,
        @RequestParam(required = false) loginType: Int?,
        @RequestParam(required = false) status: Int?,
        model: Model,
    ): String {
        model.addAttribute("loginLogsPageResult", loginLogSupport.list(pageQuery, username, loginType, status))
        model.addAttribute("totalLogCount", loginLogSupport.count())
        return "sys/login-log"
    }

    @Operation(summary = "删除单条登录日志")
    @DeleteMapping("/login-log/{id}")
    @ResponseBody
    fun deleteLoginLog(@PathVariable id: Long): SaResult {
        val rows = loginLogSupport.deleteByIds(listOf(id))
        return SaResult.ok("已删除 $rows 条记录")
    }

    @Operation(summary = "批量删除登录日志", description = "ids 为逗号分隔的 ID 列表")
    @DeleteMapping("/login-log/batch")
    @ResponseBody
    fun batchDeleteLoginLog(@RequestParam("ids") ids: String): SaResult {
        val idList = ids.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.toLong() }
        val rows = loginLogSupport.deleteByIds(idList)
        return SaResult.ok("已删除 $rows 条记录")
    }

    @Operation(summary = "清理 N 天前的登录日志")
    @DeleteMapping("/login-log/clean/older-than")
    @ResponseBody
    fun cleanLoginLogOlderThan(@RequestParam days: Int): SaResult {
        val rows = loginLogSupport.deleteOlderThanDays(days)
        return SaResult.ok("已清理 $rows 条（${days} 天前）")
    }

    @Operation(summary = "保留最新 N 条登录日志", description = "删除其余记录")
    @DeleteMapping("/login-log/clean/keep-latest")
    @ResponseBody
    fun cleanLoginLogKeepLatest(@RequestParam count: Int): SaResult {
        val rows = loginLogSupport.deleteKeepLatest(count)
        return SaResult.ok("已清理 $rows 条，保留最新 $count 条")
    }

    @Operation(summary = "清空全部登录日志")
    @DeleteMapping("/login-log/clean/all")
    @ResponseBody
    fun cleanLoginLogAll(): SaResult {
        val rows = loginLogSupport.deleteAll()
        return SaResult.ok("已清空全部 $rows 条记录")
    }

    @Operation(summary = "物理删除用户", description = "后台管理用，不可恢复")
    @DeleteMapping("/user/{id}")
    @ResponseBody
    fun deleteUser(@PathVariable("id") id: Long): SaResult {
        usersService.adminDelete(id)
        return SaResult.ok("删除成功")
    }

    @Operation(summary = "物理删除角色", description = "后台管理用，不可恢复")
    @DeleteMapping("/role/{id}")
    @ResponseBody
    fun deleteRole(@PathVariable("id") id: Long): SaResult {
        rolesService.adminDelete(id)
        return SaResult.ok("删除成功")
    }

    @Operation(summary = "物理删除权限", description = "后台管理用，不可恢复")
    @DeleteMapping("/permission/{id}")
    @ResponseBody
    fun deletePermission(@PathVariable("id") id: Long): SaResult {
        permissionsService.adminDelete(id)
        return SaResult.ok("删除成功")
    }

    companion object {
        private val log = LoggerFactory.getLogger(WebController::class.java)
    }
}
