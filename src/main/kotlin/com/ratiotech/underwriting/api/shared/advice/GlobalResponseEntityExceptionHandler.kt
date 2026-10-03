package com.ratiotech.underwriting.api.shared.advice

import com.ratiotech.underwriting.api.shared.constants.Constants
import java.lang.Exception
import kotlin.apply
import kotlin.collections.distinct
import kotlin.collections.map
import kotlin.collections.toList
import kotlin.jvm.java
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

abstract class GlobalResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {

  override fun handleMethodArgumentNotValid(
    ex: MethodArgumentNotValidException,
    headers: HttpHeaders,
    status: HttpStatusCode,
    request: WebRequest,
  ): ResponseEntity<Any>? {
    val errorMessages: List<ErrorModel> =
      ex.bindingResult.fieldErrors
        .map { err: FieldError ->
          ErrorModel(
            fieldName = err.field,
            rejectedValue = err.rejectedValue,
            messageError = err.defaultMessage,
          )
        }
        .distinct()
        .toList()
    val problem =
      ProblemDetail.forStatus(status).apply {
        detail = ex.message
        setProperty("errorMessages", errorMessages)
      }
    return handleExceptionInternal(ex, problem, HttpHeaders(), HttpStatus.BAD_REQUEST, request)
  }

  override fun handleExceptionInternal(
    ex: Exception,
    body: Any?,
    headers: HttpHeaders,
    statusCode: HttpStatusCode,
    request: WebRequest,
  ): ResponseEntity<Any>? {
    val finalBody =
      when (body) {
        null -> {
          ProblemDetail.forStatus(statusCode).apply {
            detail = ex.message ?: Constants.GENERIC_ERROR_MESSAGE
          }
        }
        is String -> {
          ProblemDetail.forStatus(statusCode).apply { detail = body }
        }
        else -> {
          body
        }
      }

    log.error("Exception intercepted in GlobalControllerAdvice: {}", ex.message, ex)

    return super.handleExceptionInternal(ex, finalBody, headers, statusCode, request)
  }

  companion object {
    private val log = LoggerFactory.getLogger(GlobalResponseEntityExceptionHandler::class.java)
  }
}
