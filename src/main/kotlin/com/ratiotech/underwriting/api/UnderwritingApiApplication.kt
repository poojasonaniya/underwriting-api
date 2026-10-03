package com.ratiotech.underwriting.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication(scanBasePackages = ["com.ratiotech"])
@EnableJpaRepositories(basePackages = ["com.ratiotech.underwriting.api.repositories"])
@EnableJpaAuditing
@EnableRetry
class UnderwritingApiApplication {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      runApplication<UnderwritingApiApplication>(*args)
    }
  }
}
