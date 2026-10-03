package com.ratiotech.underwriting.api.clients.experian.models

/**
 * Business header information from Experian.
 */
data class ExperianBusinessHeader(
  val bin: String? = null,
  val businessName: String? = null,
  val address: String? = null,
  val phone: String? = null,
  val taxId: String? = null,
  val websiteUrl: String? = null,
  val legalBusinessName: String? = null,
  val dbaNames: List<String>? = emptyList(),
)
