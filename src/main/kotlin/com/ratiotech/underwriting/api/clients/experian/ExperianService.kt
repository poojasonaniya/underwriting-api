package com.ratiotech.underwriting.api.clients.experian

import com.ratiotech.underwriting.api.clients.experian.models.ExperianCommercialScoresResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange

/**
 * HTTP service interface for Experian Commercial Scores API.
 *
 * This interface defines the HTTP endpoints for communicating with the Experian API.
 * Spring's HttpServiceProxyFactory will create an implementation at runtime.
 *
 * API Specification:
 * - Endpoint: GET /api/v1/credit-score
 * - Query Parameter: taxIdentifier (required)
 * - Response: JSON containing credit score and business information
 */
interface ExperianService {

  /**
   * Retrieves commercial credit score information for a business.
   *
   * Endpoint: GET /api/v1/credit-score?taxIdentifier={taxIdentifier}
   *
   * @param taxIdentifier The business tax identifier to look up
   * @return ExperianCommercialScoresResponse containing credit score and business details
   */
  @GetExchange(value = "/api/v1/credit-score")
  fun getCreditScore(
    @RequestParam(value = "taxIdentifier", required = true) taxIdentifier: String
  ): ExperianCommercialScoresResponse
}
