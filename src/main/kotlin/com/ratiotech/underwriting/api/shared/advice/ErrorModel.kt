package com.ratiotech.underwriting.api.shared.advice

data class ErrorModel(
  val fieldName: String? = null,
  val rejectedValue: Any? = null,
  val messageError: String? = null,
)
