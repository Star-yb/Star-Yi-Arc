# 通用 CRUD 用法

标准资源是三样东西：Jimmer 实体、`src/main/dto` 里的 `.dto`、一个继承 `JimmerCrudResource` 的 Controller。用户、角色、权限不走这里。

实现在 `yi-common` 的 `com.star.common.crud`。对照实现是 `yi-demo` 的公告。

| 文件 | 作用 |
|------|------|
| `entity/DemoNotice.kt` | 表 `demo_notice` |
| `dto/DemoNotice.dto` | 查看、创建、更新、查询条件 |
| `controller/DemoNoticeController.kt` | `@CrudAll`，并覆盖了详情 |

建表语句在 `sql/mysql.sql` 最后一段。改用 PostgreSQL 时，同一张表在 `sql/pgsql.sql` 末尾。改了 `.dto` 而没有改 Kotlin 文件时，要执行 `gradle :yi-demo:kspKotlin`，否则生成类不会更新。

## 1. 实体

需要审计字段时继承 `BaseEntity`。主键、逻辑删除和列名跟随项目里已有的实体。公告的主键是数据库自增。

## 2. DTO

```text
export com.star.demo.entity.DemoNotice
    -> package com.star.demo.dto

DemoNoticeView {
    #allScalars
    -deletedTime
}

input DemoNoticeCreateInput {
    title
    content
    pinned
}

input DemoNoticeUpdateInput {
    id?
    title
    content
    pinned
}

specification DemoNoticeSpecification {
    like/i(title)
    pinned
}
```

已经可空的字段不要再写 `?`。更新入参里的 `id?` 是给 `bindId` 用的，请求体不必自己带 id。

## 3. Controller

类上的注解决定五条基础接口里开放哪几条。没写进注解的不会注册。类上另外写的方法照常注册。

| 注解 | 开放 |
|------|------|
| `@CrudReadable` | 列表、详情 |
| `@CrudWritable` | 创建、更新、删除 |
| `@CrudAll` | 五条都有 |
| `@CrudEndpoints(...)` | 自己列出 `CrudAction` |

```kotlin
@RestController
@RequestMapping("/demo/notices")
@CrudAll
class DemoNoticeController(
    sql: KSqlClient,
) : JimmerCrudResource<
    DemoNotice,
    Long,
    DemoNoticeCreateInput,
    DemoNoticeUpdateInput,
    DemoNoticeSpecification,
    >(
    sql,
    DemoNotice::class,
    DemoNoticeView.METADATA.fetcher,
) {
    override fun bindId(id: Long, input: DemoNoticeUpdateInput): DemoNoticeUpdateInput =
        input.copy(id = id)
}
```

更新对象是不可变的。`bindId` 用 `copy` 把路径上的 id 放进更新对象。不覆盖的话，更新时 id 进不去。

空 Controller 加上注解之后，路由是：

| 动作 | 方法 | 路径 |
|------|------|------|
| 列表 | GET | `/demo/notices` |
| 详情 | GET | `/demo/notices/{id}` |
| 创建 | POST | `/demo/notices` |
| 更新 | PUT | `/demo/notices/{id}` |
| 删除 | DELETE | `/demo/notices/{id}` |

默认行为：

| 动作 | 行为 |
|------|------|
| 创建 | `INSERT_ONLY`。没有写入行时 400 |
| 更新 | `UPDATE_ONLY`。目标不存在时 400 |
| 删除 | `DeleteMode.AUTO`。继承了 `BaseEntity` 的做逻辑删除 |
| 详情 | 按 id 和 fetcher 查，没有则 404 |
| 列表 | `PageQuery`。页码从 1 开始，缺省或小于 1 时是第 1 页 |

创建、更新、删除前后各有一个空钩子：`beforeCreate`、`afterCreate`、`beforeUpdate`、`afterUpdate`、`beforeDelete`、`afterDelete`。只加校验或副作用时覆盖钩子，不必重写整段保存。

## 4. 改其中一条

覆盖方法即可，路径仍是原来的。要换整段 HTTP，覆盖 `list`、`obtainById`、`createOne`、`updateById` 或 `deleteById`。只换查询或保存，覆盖 `page`、`obtain`、`create`、`update` 或 `delete`。

公告覆盖的是详情的 HTTP 方法。先打印，再走原来的查询：

```kotlin
override fun obtainById(id: Long): SaResult {
    println("查询详情方法被重构了")
    return super.obtainById(id)
}
```

覆盖后的方法里可以调用自己的 Service，也可以使用对子类可见的 `sql`。

## 5. 额外接口

不是五条基础动作的方法，不要加 `@CrudOperation`。直接写在同一个 Controller 上。

公告有两条：

| 方法 | 路径 | 作用 |
|------|------|------|
| GET | `/demo/notices/pinned` | 查出 `pinned = true` 的公告 |
| PUT | `/demo/notices/{id}/pin` | 只改置顶。请求体是 `DemoNoticePinRequest` |

`/pinned` 要写成明确路径，避免被 `/{id}` 吃掉。设置置顶不走通用更新，所以前端不必提交标题和正文。
