package com.ratiotech.underwriting.api.controllers.responses

import java.util.Date
import java.util.UUID

data class Customer(
  val id: UUID?,
  val name: String?,
  val taxIdentifier: String?,
  val createdDate: Date?,
  val createdBy: UUID?,
  val lastModifiedBy: UUID?,
  val lastModifiedDate: Date?,
)
