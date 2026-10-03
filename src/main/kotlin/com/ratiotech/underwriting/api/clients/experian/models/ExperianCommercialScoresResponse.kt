package com.ratiotech.underwriting.api.clients.experian.models

/**
 * Response model for Experian Commercial Scores API.
 */
data class ExperianCommercialScoresResponse(
  val requestId: String? = null,
  val success: Boolean? = null,
  val result: ExperianCommercialScoresResults? = null,
)
