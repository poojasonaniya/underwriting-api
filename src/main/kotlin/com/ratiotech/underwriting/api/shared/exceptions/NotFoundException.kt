package com.ratiotech.underwriting.api.shared.exceptions

open class NotFoundException : RuntimeException {
  constructor() : super()

  constructor(message: String) : super(message)

  constructor(message: String, t: Throwable) : super(message, t)
}
