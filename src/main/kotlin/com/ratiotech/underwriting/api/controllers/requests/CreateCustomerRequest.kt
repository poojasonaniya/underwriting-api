package com.ratiotech.underwriting.api.controllers.requests

import jakarta.validation.constraints.NotEmpty

data class CreateCustomerRequest(
  @field:NotEmpty(message = "'name' is required") val name: String,
  @field:NotEmpty(message = "'taxIdentifier' is required") val taxIdentifier: String,
)
