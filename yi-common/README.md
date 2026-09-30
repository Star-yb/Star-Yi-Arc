# yi-common

`yi-common` 是公共库，不能单独启动。`yi-admin` 依赖它，并扫描 `com.star`，所以这里的 `@Component`、`@RestControllerAdvice` 会在宿主启动时生效。

这里只放和具体业务页面无关、多个模块都会用到的类型：系统实体、Jimmer 基础设施、Sa-Token 的共享监听、异常、分页、OpenAPI 常量、HTTP 与 Excel 工具。

启动期装配不在本模块。Sa-Token 拦截器、Thymeleaf 方言、Jimmer 列名策略在 `yi-admin` 的 `com.star.config`。

源码在 `src/main/kotlin`。`build/generated` 里的 Draft、Fetcher、Props 是 Jimmer 生成物，不在这份清单里登记，也不要手改。

新增文件时，在下面对应技术的表里加一行。不要另开一份不分类的文件清单。

## 目录

```text
src/main/kotlin/com/star
├── model/entity          实体公共字段
├── identity/entity       用户、角色、权限、登录日志
├── common/jimmer         Jimmer 标量映射与主键生成
├── common/satoken        Sa-Token 共享监听
├── admin/web             超级管理员侧栏扩展点
├── common/exception      业务异常
├── common/globalexception 全局异常出口
├── common/page           分页入参和返回
├── common/crud           通用增删改查
│   ├── annotation        开放哪些接口
│   ├── operation         五条基础接口
│   ├── JimmerCrudResource.kt  默认保存和查询
│   └── web               按注解注册路由
├── openapi               接口文档常量
└── utils                 密码、HTTP、Excel
```

## Jimmer

父工程依赖 `jimmer-spring-boot-starter:0.12.2`。实体是 Kotlin 接口。列名默认小写加下划线，由 `yi-admin` 的 `JimmerConfigure` 决定；写了 `@Column` 的字段不受这个策略影响。

| 文件 | 作用 |
|------|------|
| `model/entity/BaseEntity.kt` | 公共字段：`createdTime`、`modifiedTime`、`deletedTime`、`deleted`。`deleted = true` 时按逻辑删除，普通查询不会查出这些行 |
| `identity/entity/Users.kt` | 表 `sys_users`。主键由 `VerifiableOrderedIdGenerator` 生成，不用数据库自增。角色经 `sys_user_role` 关联 |
| `identity/entity/Roles.kt` | 表 `sys_roles`，主键自增。用户和权限的多对多维护端不在这张表上 |
| `identity/entity/Permissions.kt` | 表 `sys_permissions`，主键自增。`permissionType`：1 菜单，2 按钮，3 接口。`parent` 组成树。与角色的中间表是 `sys_role_permission` |
| `identity/entity/LoginLogs.kt` | 表 `sys_login_log`。只追加，不继承 `BaseEntity`，不做逻辑删除。`loginType`：1 登录，2 登出。`status`：0 成功，1 失败 |
| `common/jimmer/VerifiableOrderedIdGenerator.kt` | 给 `Users` 生成可校验的有序 Long。组成是版本、相对秒、机器号、序号和 4 位校验。校验盐来自系统属性 `ed.id.salt` |
| `common/jimmer/UuidScalarProvider.kt` | 实体属性是 `UUID`、数据库列是字符串时，读写都走这里 |

用户、角色、权限互相引用，必须放在同一个模块。登录日志单向指向用户，和这组系统表放在一起。

## Sa-Token

父工程依赖 `sa-token-spring-boot4-starter:1.46.0`。登录拦截、JWT、跨域在 `yi-admin` 的 `com.star.config.satoken`，不在这里。

| 文件 | 作用 |
|------|------|
| `common/satoken/MySaTokenListener.kt` | 登录成功时进入 `doLogin`。目前只打日志，后面可在这里写登录日志或更新最后登录时间 |
| `utils/AuthCryptoUtils.kt` | 密码 SHA-256。入库用 `hash`，登录用 `matches`。没有加盐 |

`StpInterface` 在 `yi-admin` 的 `com.star.admin.auth`，不在本模块。

## 后台菜单

侧栏菜单的扩展点放在这里，具体页面和模板在 `yi-admin`。

| 文件 | 作用 |
|------|------|
| `admin/web/AdminPageModule.kt` | 业务模块实现这个接口来登记菜单。路径用来高亮当前页。同文件里有 `AdminMenuItem`、`AdminMenuCategory` |

## 异常

| 文件 | 作用 |
|------|------|
| `common/exception/BusinessException.kt` | 可预期的业务失败。默认业务码 500，用 `code()` 改成 400 等 |
| `common/globalexception/GlobalExceptionHandler.kt` | 把 Controller 抛出的异常收成 `SaResult`。Sa-Token 的 30001、11011、11012、11013、11041 有单独中文文案；`BusinessException` 使用异常上的业务码 |

## 分页

页码从 1 开始。没传或小于 1 时按第 1 页，不会因此把整表查出来。Spring Data 内部从 0 开始，转换在 `PageResult.ofPaged` 里完成。总数和当前页由 Jimmer 的 `fetchSpringPage` 各查一次。

| 文件 | 作用 |
|------|------|
| `common/page/PageQuery.kt` | 入参：`page`、`size`、`sort`、`order`。`sort` 可用逗号分隔多个字段，字段后可写 `:asc` 或 `:desc`。为空时按 `id` 升序 |
| `common/page/PageResult.kt` | 页面和接口共用的返回。`ofPaged` 接 Spring Data 的一页；`ofUnpaged` 只给仍要套进同一套字段的整表结果 |

## 通用 CRUD

按注解、五条接口、默认实现、路由注册分成四个包。Controller 继承 `JimmerCrudResource`，用注解决定开放哪几条。要改某一条，在 Controller 里覆盖那个方法，里面再调用自己的 Service。用户、角色、权限不走这里。

| 文件 | 作用 |
|------|------|
| `common/crud/annotation/CrudAction.kt` | 五个动作：列表、详情、创建、更新、删除 |
| `common/crud/annotation/CrudEndpoints.kt` | `@CrudEndpoints` 指定动作。`@CrudReadable` 只有查，`@CrudWritable` 只有写，`@CrudAll` 都有 |
| `common/crud/annotation/CrudOperation.kt` | 标在五条基础方法上，注册时用来识别 |
| `common/crud/operation/CrudList.kt` | 分页列表 |
| `common/crud/operation/CrudObtain.kt` | 按主键查询 |
| `common/crud/operation/CrudCreate.kt` | 创建 |
| `common/crud/operation/CrudUpdate.kt` | 更新 |
| `common/crud/operation/CrudDelete.kt` | 删除 |
| `common/crud/JimmerCrudResource.kt` | 五条接口的默认保存和查询，以及 `beforeCreate` 等钩子 |
| `common/crud/web/CrudEndpointHandlerMapping.kt` | 按注解注册路由。Controller 上没有的动作不会成为接口 |

## OpenAPI

本模块依赖 `swagger-annotations-jakarta:2.2.30`，只提供注解常量。扫描和 UI 在 `yi-admin` 的 springdoc 配置里。

| 文件 | 作用 |
|------|------|
| `openapi/ApiDocConstants.kt` | `SECURITY_SCHEME_SATOKEN = "satoken"`。接口上的 `@SecurityRequirement` 用这个名字，对应请求头 `satoken` |

## HTTP

使用父工程已经带入的 Spring `RestClient`，不再单独引入 HTTP 客户端。连接和读取各 30 秒。

| 文件 | 作用 |
|------|------|
| `utils/HttpResult.kt` | 调用结果。`statusCode` 是 HTTP 状态码，`body` 是响应原文。2xx 视为成功 |
| `utils/RestClientUtils.kt` | GET、JSON POST、表单 POST。4xx 和 5xx 也返回结果。网络失败抛 `UncheckedIOException` |

## Excel

本模块依赖 `fastexcel:0.20.2`，只负责写出 xlsx。

| 文件 | 作用 |
|------|------|
| `utils/ExcelUtils.kt` | 按 `Column` 从对象抽列并下载。第一行是表头，空数据只写表头 |

## 依赖从哪来

本模块 `build.gradle.kts` 自己声明的：

| 依赖 | 用途 |
|------|------|
| `swagger-annotations-jakarta:2.2.30` | OpenAPI 注解 |
| `fastexcel:0.20.2` | Excel 导出 |

父工程 `subprojects` 带进来、本模块源码直接用到的：

| 依赖 | 用途 |
|------|------|
| `spring-boot-starter-web` | `GlobalExceptionHandler`、`ExcelUtils` 的 Servlet 类型、`RestClient` |
| `sa-token-spring-boot4-starter:1.46.0` | 登录监听、`SaResult`、密码摘要 |
| `jimmer-spring-boot-starter:0.12.2` | 实体与标量映射 |
| `spring-data-commons`（传递依赖） | `Page`、`Pageable`、`Sort` |
