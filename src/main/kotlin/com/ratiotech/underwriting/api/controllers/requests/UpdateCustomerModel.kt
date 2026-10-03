package com.ratiotech.underwriting.api.controllers.requests

/**
 * Model for updating customer information via JSON Patch operations. Contains all fields that can
 * be modified for a customer.
 *
 * @param name the customer's name
 * @param taxIdentifier the customer's tax identifier
 */
data class UpdateCustomerModel(val name: String?, val taxIdentifier: String?)
