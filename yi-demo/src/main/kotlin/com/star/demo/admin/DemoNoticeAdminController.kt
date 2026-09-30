package com.star.demo.admin

import com.star.common.page.PageQuery
import com.star.demo.controller.DemoNoticeController
import com.star.demo.dto.DemoNoticeSpecification
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/** 演示公告的超级管理员页面。模板在本模块 resources/admin/。 */
@Tag(name = "超级管理员-演示公告", description = "DemoNotice Thymeleaf 管理页")
@Controller
@RequestMapping("/admin/demo/notices")
class DemoNoticeAdminController(
    private val demoNoticeController: DemoNoticeController,
) {

    @Operation(summary = "演示公告管理页")
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
