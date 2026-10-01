# 待查看的审查记录

2026-10-01 用 Open Code Review 的 Delegation 模式扫过全仓库。高优先级里，禁用用户不注销 token 已经改掉，见 `UsersServiceImpl.disableUser`。权限注解、测试接口、密码加盐、本地连接配置按当时的决定先不动。

下面是还没改的中等问题，方便之后逐条决定。

逻辑删除 `DELETE /users/{id}` 和后台物理删除目前也不会注销 token。这次只加在禁用上。

## 登录与鉴权

| 位置 | 现象 |
|------|------|
| `AuthServiceImpl.login` | 账号不存在时返回「账号不存在」，密码错误时返回「账号或密码错误」。调用方可以据此判断用户名或手机号是否存在 |
| `AuthController` `POST /auth/verify` | `/auth/**` 整段免登录。拿到任意 token 就能查到是否过期、登录 id、账号是否启用 |
| `SaTokenConfigure.excludePaths` | `/swagger-ui.html`、`/swagger-ui/**`、`/v3/api-docs/**` 免登录，接口清单对外可见 |
| `SaTokenConfigure.getSaServletFilter` | 所有响应都写 `Access-Control-Allow-Origin: *`，并允许任意方法和请求头。调试日志会打印提交的 token |
| `WebController` `GET /admin/logout`、`AuthController` `GET /auth/logout` | 退出是 GET。浏览器会自动带上 Cookie 时，其它页面有机会顺带触发退出 |

## 异常与日志

| 位置 | 现象 |
|------|------|
| `GlobalExceptionHandler` | 未单独分类的异常把 `exception.message` 返回给调用方，内部信息可能漏出去。`NotLoginException` 是 `SaTokenException` 的子类，Spring 会进更具体的那个处理方法，11011、11012、11013 的中文文案实际走不到 |
| `LoginLogRecorder.resolveIp` | 优先采用 `X-Forwarded-For` 和 `X-Real-IP`，前面没有可信代理校验。登录日志里的 IP 可以被请求头改掉 |

## 接口语义

| 位置 | 现象 |
|------|------|
| `UsersController.updateRole` | 路径上的用户 id 没有写回请求体，实际以 body 里的 `id` 为准 |
| `RolesController.updatePermission` | 同样忽略路径上的角色 id，以 body 里的 `id` 为准。后台页面两边传的是同一个 id，所以页面现在能保存成功 |
| `JimmerCrudResource.bindId` | 默认原样返回入参，不把路径 id 放进去。`DemoNoticeController` 自己覆盖了。以后新的 CRUD 资源如果忘了覆盖，更新会作用到 body 里的 id |
| `UsersController.getRoleUsers` | 接口说明是「查询用户已绑定角色」，实现是按角色 id 查出该角色下的用户。真正按用户查角色的是 `GET /roles/user/{userId}` |

## 页面与查询

| 位置 | 现象 |
|------|------|
| `templates/sys/permission-list.html` 的 `renderSelectedTags` | 权限名用 `innerHTML` 拼进页面。名称里如果带 HTML，会在这个管理页里执行。列表正文用的是 `th:text`，那一处是转义过的 |
| `PageQuery.toPageable` | `size` 只把小于等于 0 改成 10，没有上限。调用方可以一次把整表拉走 |

## ID 生成

`VerifiableOrderedIdGenerator` 的注释写的是：时钟小幅回拨会等待，超过 5 毫秒就拒绝。

实现里比较的是 epoch 秒。秒数一旦倒退，差值乘 1000 至少是 1000 毫秒，一定大于 5，等待分支进不去。不到 1 秒的回拨因为秒数没变，也不会被发现。
