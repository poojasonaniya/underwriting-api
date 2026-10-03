package com.ratiotech.underwriting.api.clients.experian.models

/**
 * Commercial score information from Experian.
 */
data class ExperianScore(
  val score: Long? = null,
  val recommendedCreditLimitAmount: Long? = null,
)
