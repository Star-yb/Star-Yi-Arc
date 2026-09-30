package com.star.utils

import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException
import java.io.UncheckedIOException
import java.net.URI
import java.net.http.HttpClient
import java.nio.charset.StandardCharsets
import java.time.Duration

/**
 * 同步 HTTP 调用，使用 Spring 的 RestClient。
 * 连接和读取各 30 秒。4xx 和 5xx 也返回 [HttpResult]，网络失败抛 [UncheckedIOException]。
 * 当前项目还没有业务代码调用这里。
 */
object RestClientUtils {

    private val client: RestClient = RestClient.builder()
        .requestFactory(
            JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build(),
            ).apply {
                setReadTimeout(Duration.ofSeconds(30))
            },
        )
        .build()

    fun get(
        url: String,
        query: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
    ): HttpResult = execute(HttpMethod.GET, url, query, headers)

    /** json 为空时发送空字符串。按原文送出，不再包一层 JSON 字符串。 */
    fun postJson(
        url: String,
        json: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): HttpResult = execute(HttpMethod.POST, url, headers = headers) {
        contentType(MediaType.APPLICATION_JSON)
        body((json ?: "").toByteArray(StandardCharsets.UTF_8))
    }

    /** application/x-www-form-urlencoded，UTF-8。 */
    fun postForm(
        url: String,
        formFields: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
    ): HttpResult = execute(HttpMethod.POST, url, headers = headers) {
        contentType(MediaType.APPLICATION_FORM_URLENCODED)
        body(
            LinkedMultiValueMap<String, String>().apply {
                formFields.forEach { (name, value) -> add(name, value) }
            },
        )
    }

    private fun execute(
        method: HttpMethod,
        url: String,
        query: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        prepare: RestClient.RequestBodySpec.() -> Unit = {},
    ): HttpResult {
        try {
            return client.method(method)
                .uri(toUri(url, query))
                .headers { target ->
                    headers.forEach { (name, value) -> target.add(name, value) }
                }
                .apply(prepare)
                .exchange { _, response ->
                    HttpResult(response.statusCode.value(), response.bodyTo(String::class.java) ?: "")
                }
        } catch (exception: ResourceAccessException) {
            val cause = exception.cause
            if (cause is IOException) {
                throw UncheckedIOException(cause)
            }
            throw exception
        }
    }

    private fun toUri(url: String, query: Map<String, String>): URI =
        UriComponentsBuilder.fromUriString(url).apply {
            query.forEach { (name, value) -> queryParam(name, value) }
        }.encode().build().toUri()
}
