package com.ratiotech.underwriting.api.shared.advice

import com.ratiotech.underwriting.api.shared.constants.Constants
import com.ratiotech.underwriting.api.shared.exceptions.BadRequestException
import com.ratiotech.underwriting.api.shared.exceptions.NotFoundException
import jakarta.validation.ConstraintViolationException
import kotlin.apply
import kotlin.collections.map
import kotlin.jvm.java
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
class GlobalControllerAdvice : GlobalResponseEntityExceptionHandler() {

  @ExceptionHandler(Exception::class)
  fun serverExceptionHandler(exception: Exception, request: WebRequest): ResponseEntity<Any>? {
    log.error(exception.message, exception)
    // this is a catch-all method if we don't have another more explicit handler below
    return handleExceptionInternal(
      exception,
      Constants.GENERIC_ERROR_MESSAGE,
      HttpHeaders(),
      HttpStatus.INTERNAL_SERVER_ERROR,
      request,
    )
  }

  @ExceptionHandler(BadRequestException::class)
  fun badRequestExceptionHandler(
    exception: BadRequestException,
    request: WebRequest,
  ): ResponseEntity<Any>? =
    handleExceptionInternal(
      exception,
      exception.message,
      HttpHeaders(),
      HttpStatus.BAD_REQUEST,
      request,
    )

  @ExceptionHandler(NotFoundException::class)
  fun notFoundExceptionHandler(
    exception: NotFoundException,
    request: WebRequest,
  ): ResponseEntity<Any>? =
    handleExceptionInternal(
      exception,
      exception.message,
      HttpHeaders(),
      HttpStatus.NOT_FOUND,
      request,
    )

  @ExceptionHandler(NoSuchElementException::class)
  fun noSuchElementExceptionHandler(
    exception: NoSuchElementException,
    request: WebRequest,
  ): ResponseEntity<Any>? =
    handleExceptionInternal(
      exception,
      exception.message,
      HttpHeaders(),
      HttpStatus.NOT_FOUND,
      request,
    )

  @ExceptionHandler(ConstraintViolationException::class)
  fun constraintViolationExceptionHandler(
    ex: ConstraintViolationException,
    request: WebRequest,
  ): ResponseEntity<Any>? {
    val errorMessages =
      ex.constraintViolations.map {
        ErrorModel(
          fieldName = it.propertyPath.toString(),
          rejectedValue = it.invalidValue,
          messageError = it.message,
        )
      }

    val problem =
      ProblemDetail.forStatus(HttpStatus.BAD_REQUEST).apply {
        detail = "Validation error"
        setProperty("errorMessages", errorMessages)
      }
    return handleExceptionInternal(ex, problem, HttpHeaders(), HttpStatus.BAD_REQUEST, request)
  }

  companion object {
    private val log = LoggerFactory.getLogger(GlobalControllerAdvice::class.java)
  }
}
