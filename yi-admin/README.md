# yi-admin

`yi-admin` 是唯一能启动的模块。它依赖 `yi-common` 和 `yi-demo`，扫描 `com.star`，所以这两个库里的 `@Component`、`@Controller` 和 `@RestController` 会在这里生效。

启动类是 `com.star.YiAdminApplication`。端口、数据库、Redis、Sa-Token 和 springdoc 写在 `src/main/resources/application.properties`。库模块不要再放一份同名文件。

项目说明在仓库根目录的 `README.md`。本文件只登记这个模块里的源码。

源码在 `src/main/kotlin`。`build/generated` 里的 DTO 是 Jimmer 生成物，不在这份清单里登记，也不要手改。DTO 定义在 `src/main/dto`。

新增文件时，在下面对应的表里加一行。

## 目录

```text
src/main/kotlin/com/star
├── Application.kt            启动类
├── config                    启动横幅、Jimmer、Sa-Token
├── admin/auth                登录后的角色和权限
├── admin/web                 侧栏收集、模板位置、内置菜单
├── admin/controller          页面、认证、用户、角色、权限
├── admin/service             上述接口的业务
├── admin/dao                 Jimmer 访问
└── admin/support             登录会话和登录日志
src/main/dto                  用户、角色、权限、登录的 Jimmer DTO
src/main/resources
├── application.properties
├── templates                 登录页和系统管理页
└── static                    样式和图
```

## 启动与配置

| 文件 | 作用 |
|------|------|
| `Application.kt` | 扫描 `com.star` |
| `config/ApplicationStartupListener.kt` | 就绪后打印端口、接口根路径、Swagger 和登录页 |
| `config/jimmer/JimmerConfigure.kt` | 未写 `@Column` 的列名用小写加下划线 |
| `config/satoken/SaTokenConfigure.kt` | JWT、登录拦截、`/admin/**` 要求角色 `*`、文档和 `/auth/**` 放行 |
| `config/satoken/SaTokenDaoConfigure.kt` | Redis 更新会话时保留剩余过期时间 |
| `config/satoken/SaTokenThymeleafConfigure.kt` | 页面上的 Sa-Token 方言 |

## 认证和菜单

| 文件 | 作用 |
|------|------|
| `admin/auth/StpInterfaceImpl.kt` | 超级管理员返回 `*`。普通账号只取状态为 0 的角色和权限 |
| `admin/auth/SaAuthCache.kt` | 普通账号的角色、权限缓存键 |
| `admin/web/AdminMenuRegistry.kt` | 按模块顺序排分组，并按当前路径匹配菜单 |
| `admin/web/AdminMenuModelAdvice.kt` | 给 `/admin` 页面注入侧栏和当前菜单。登录页除外 |
| `admin/web/AdminThymeleafConfigure.kt` | 业务 JAR 的模板放在 `classpath:/admin/` |
| `admin/web/SysAdminPageModule.kt` | 用户、角色、权限、登录日志、接口文档 |

页面怎么接，见 [docs/admin-pages.md](../docs/admin-pages.md)。

## 控制器

| 文件 | 作用 |
|------|------|
| `admin/controller/webpage/WebController.kt` | `/admin` 下的登录、系统页面，以及登录日志和删除操作 |
| `admin/controller/auth/AuthController.kt` | `/auth` 登录、登出、校验。只处理 `api` 会话 |
| `admin/controller/sys/UsersController.kt` | `/users` |
| `admin/controller/sys/RolesController.kt` | `/roles` |
| `admin/controller/sys/PermissionsController.kt` | `/permissions` |
| `admin/controller/UsersTestController.kt` | `/test/users`，免登录 |

接口清单见仓库根目录的 [README.md](../README.md)。

## 业务和数据

| 文件 | 作用 |
|------|------|
| `admin/service/AuthService.kt` | 登录契约 |
| `admin/service/UsersService.kt` | 用户契约 |
| `admin/service/RolesService.kt` | 角色契约 |
| `admin/service/PermissionsService.kt` | 权限契约 |
| `admin/service/impl/AuthServiceImpl.kt` | 校验账号密码并记录登录 |
| `admin/service/impl/UsersServiceImpl.kt` | 用户保存、角色和导出用的查询 |
| `admin/service/impl/RolesServiceImpl.kt` | 角色和角色权限 |
| `admin/service/impl/PermissionsServiceImpl.kt` | 权限树和按角色取权限 |
| `admin/dao/UsersDao.kt` | 用户表访问 |
| `admin/dao/RolesDao.kt` | 角色表访问 |
| `admin/dao/PermissionsDao.kt` | 权限表访问 |
| `admin/support/SaAuthLoginSupport.kt` | 后台 Cookie 会话用设备 `admin-web`，避免盖住接口 token |
| `admin/support/LoginLogRecorder.kt` | 写登录、失败和登出 |
| `admin/support/LoginLogSupport.kt` | 登录日志的分页和清理。默认按登录时间倒序 |

## DTO

| 文件 | 作用 |
|------|------|
| `dto/Auth.dto` | 登录和校验的入参、出参 |
| `dto/User.dto` | 用户的查看、创建、更新、角色更新和查询条件 |
| `dto/Role.dto` | 角色的查看、创建、更新、权限更新和查询条件 |
| `dto/Permission.dto` | 权限的查看、创建、更新和查询条件 |

改密码不用 Jimmer Input，请求体是 `UsersController` 里的 `ChangePasswordRequest`。

## 页面和静态资源

| 路径 | 作用 |
|------|------|
| `templates/login.html` | 登录页 |
| `templates/common/fragments.html` | 侧栏、顶栏、分页和公共脚本 |
| `templates/sys/user.html` | 用户管理 |
| `templates/sys/role.html` | 角色管理 |
| `templates/sys/role-permission.html` | 为角色分配权限 |
| `templates/sys/permission.html` | 权限树 |
| `templates/sys/permission-list.html` | 权限平铺列表 |
| `templates/sys/login-log.html` | 登录日志 |
| `templates/sys/api-docs.html` | 内嵌 Swagger |
| `static/css/admin.css` | 后台样式 |
| `static/css/login.css` | 登录页样式 |
| `static/img/login-bg.png` | 登录页背景 |

## 本模块额外依赖

父工程已经带入 Web、Sa-Token Spring Boot 4 starter 和 Jimmer。这里另外声明：

| 依赖 | 用途 |
|------|------|
| `yi-common`、`yi-demo` | 公共库和演示公告 |
| `spring-boot-starter-thymeleaf` | 管理页 |
| `spring-boot-starter-jdbc`、`mysql-connector-j`、`postgresql`、Druid | 默认 MySQL，PostgreSQL 驱动留着以便切换 |
| `sa-token-jwt`、`sa-token-thymeleaf`、`sa-token-redis-template` | JWT、页面标签、Redis 会话 |
| `springdoc-openapi-starter-webmvc-ui` | Swagger |
| `jackson-module-kotlin` | 反序列化 Kotlin data class |
| `spring-boot-starter-actuator` | 运行信息。不写进 OpenAPI |
