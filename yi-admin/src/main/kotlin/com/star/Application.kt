package com.star

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.star"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
