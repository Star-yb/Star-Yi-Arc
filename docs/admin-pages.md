# 管理页用法

业务模块自己登记侧栏和页面。不必改 `WebController` 或 `fragments.html`。系统里已有的页面见项目 [README](../README.md)。这里只写怎么加一页。

对照实现是 `yi-demo` 的演示公告：

| 文件 | 作用 |
|------|------|
| `yi-demo/.../admin/DemoNoticeAdminPageModule.kt` | 登记菜单 |
| `yi-demo/.../admin/DemoNoticeAdminController.kt` | 打开页面并准备数据 |
| `yi-demo/src/main/resources/admin/yi-demo/index.html` | 模板 |

## 1. 登记菜单

在业务模块里实现 `AdminPageModule`，标成 `@Component`。`yi-admin` 启动时会收到这个 Bean。

```kotlin
@Component
class DemoNoticeAdminPageModule : AdminPageModule {
    override fun moduleId(): String = "yi-demo"
    override fun moduleOrder(): Int = 90
    override fun menuItems(): List<AdminMenuItem> = listOf(
        AdminMenuItem(
            menuKey = "demo-notice",
            category = "开发演示",
            label = "演示公告",
            path = "/admin/demo/notices",
            icon = "bx bx-news",
            order = 10,
        ),
    )
}
```

`path` 以 `/admin/` 开头，并且和下面 Controller 的映射一致。`moduleOrder` 越小，这个模块带出的分组越靠前。系统菜单是 `0`，演示模块是 `90`，所以「开发演示」在系统分组后面。同一分组里的菜单再按 `order` 排。

`menuKey` 用来区分高亮。模板里不用再写它。

## 2. 写页面 Controller

这是 Thymeleaf 的 `@Controller`，不是 REST。返回视图名。

```kotlin
@Controller
@RequestMapping("/admin/demo/notices")
class DemoNoticeAdminController(
    private val demoNoticeController: DemoNoticeController,
) {
    @GetMapping
    fun index(
        pageQuery: PageQuery,
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) pinned: String?,
        model: Model,
    ): String {
        val specification = DemoNoticeSpecification(
            title = title?.takeIf { it.isNotBlank() },
            pinned = when (pinned) {
                "true" -> true
                "false" -> false
                else -> null
            },
        )
        model.addAttribute("noticesPageResult", demoNoticeController.page(pageQuery, specification))
        return "yi-demo/index"
    }
}
```

列表直接调用接口 Controller 上的 `page`，不必再写一套查询。筛选条件在这里组装，空字符串不要交给布尔字段。

`/admin/**` 已经要求登录和角色 `*`，页面方法上不必再写一遍。

## 3. 放模板

视图名 `yi-demo/index` 对应：

```text
src/main/resources/admin/yi-demo/index.html
```

`yi-admin` 的模板解析器前缀是 `classpath:/admin/`，后缀是 `.html`。系统自带页面放在 `yi-admin` 的 `templates/`，业务页面放在业务 JAR 的 `admin/`。

侧栏和顶栏不带参数。高亮和面包屑按当前地址匹配已登记的 `path`，取最长的那一条。

```html
<aside th:replace="~{common/fragments :: sidebar}"></aside>
<header th:replace="~{common/fragments :: topbar}"></header>
```

分页保留当前查询参数：

```html
<th:block th:replace="~{common/fragments :: adminPagination(${noticesPageResult}, '/admin/demo/notices')}"></th:block>
```

页尾加上 `common_modals` 和 `common_scripts`，分页按钮才能用。

子页面可以不单独登记菜单。路径落在某条菜单下面时，就会高亮那一条。例如 `/admin/role-permission` 高亮「角色管理」。如果这条路径不应该跟某条短路径绑在一起，就给它自己登记一条更长的 `path`。

## 4. 检查

1. `moduleId` 在日志里出现「已注册超级管理员页面模块」。
2. 侧栏分组和名称与 `AdminMenuItem` 一致。
3. 打开 `path` 时，这一项是高亮的，面包屑是它的分组和名称。
4. 列表的页码、筛选条件在翻页后还在。
