package com.star.utils

import jakarta.servlet.http.HttpServletResponse
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.Date

/**
 * 用 fastexcel 导出 xlsx。按列从对象取值，第一行是表头，写到响应时带附件下载头。
 */
object ExcelUtils {

    /** 一列的表头，以及从一行对象里取出单元格值的方法。 */
    data class Column<T>(
        val header: String,
        val extractor: (T) -> Any?,
    )

    /** 按列抽值后作为附件下载。data 为空时只写表头。 */
    fun <T> writeByColumnsResponse(
        response: HttpServletResponse,
        rawFileName: String?,
        sheetName: String?,
        columns: List<Column<T>>,
        data: List<T>?,
    ) {
        prepareDownloadResponse(response, rawFileName)
        Workbook(response.outputStream, "yi-admin", "1.0").use { workbook ->
            val sheet = workbook.newWorksheet(sheetName.orSheet())
            columns.forEachIndexed { columnIndex, column ->
                sheet.value(0, columnIndex, column.header)
            }
            data.orEmpty().forEachIndexed { rowIndex, item ->
                columns.forEachIndexed { columnIndex, column ->
                    writeCell(sheet, rowIndex + 1, columnIndex, column.extractor(item))
                }
            }
        }
        response.flushBuffer()
    }

    private fun writeCell(sheet: Worksheet, row: Int, column: Int, value: Any?) {
        when (value) {
            null -> sheet.value(row, column, "")
            is String -> sheet.value(row, column, value)
            is Number -> sheet.value(row, column, value)
            is Boolean -> sheet.value(row, column, value)
            is LocalDateTime -> sheet.value(row, column, value)
            is LocalDate -> sheet.value(row, column, value)
            is ZonedDateTime -> sheet.value(row, column, value)
            is Date -> sheet.value(row, column, value)
            else -> sheet.value(row, column, value.toString())
        }
    }

    private fun String?.orSheet(): String = if (isNullOrBlank()) "Sheet1" else this

    private fun prepareDownloadResponse(response: HttpServletResponse, rawFileName: String?) {
        val finalFileName = rawFileName?.takeUnless { it.isBlank() } ?: "export"
        val encoded = URLEncoder.encode("$finalFileName.xlsx", StandardCharsets.UTF_8).replace("+", "%20")
        response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        response.characterEncoding = StandardCharsets.UTF_8.name()
        response.setHeader(
            "Content-Disposition",
            "attachment; filename=\"$encoded\"; filename*=UTF-8''$encoded",
        )
    }
}
