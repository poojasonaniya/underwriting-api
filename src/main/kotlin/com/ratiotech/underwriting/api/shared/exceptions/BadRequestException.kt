package com.ratiotech.underwriting.api.shared.exceptions

open class BadRequestException(message: String?, throwable: Throwable?) :
  RuntimeException(message, throwable) {
  constructor() : this(null, null)

  constructor(message: String) : this(message, null)
}
