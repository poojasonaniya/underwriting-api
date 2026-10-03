package com.ratiotech.underwriting.api.clients.experian.models

/**
 * Results from Experian Commercial Scores API.
 */
data class ExperianCommercialScoresResults(
  val businessHeader: ExperianBusinessHeader? = null,
  val commercialScore: ExperianScore? = null,
)
