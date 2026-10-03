package com.ratiotech.underwriting.api.controllers

import com.github.fge.jsonpatch.JsonPatch
import com.ratiotech.underwriting.api.controllers.requests.CreateCustomerRequest
import com.ratiotech.underwriting.api.controllers.responses.Customer
import com.ratiotech.underwriting.api.controllers.responses.PagedResponse
import com.ratiotech.underwriting.api.logic.CustomerLogic
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Max
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import org.springframework.validation.annotation.Validated

@RestController
@Validated
class CustomerController(private val customerLogic: CustomerLogic) {

  @GetMapping(path = ["/v1/customers"])
  fun  getAllCustomers(): ResponseEntity<List<Customer>> {
    val customers = customerLogic.getAllCustomers()
    return ResponseEntity.ok(customers)
  }

  /**
   * Returns a page of customers.
   *
   * @param page the zero-indexed page number (default 0, must be >= 0)
   * @param size the number of customers per page (default 20, must be between 1 and 100)
   * @param sort optional sort expression in the form `field,direction` (e.g. `name,asc`)
   * @return 200 OK with the requested page of customers and pagination metadata
   */
  @GetMapping(path = ["/v1/customers/paged"])
  fun getCustomersPage(
    @RequestParam(defaultValue = "0") @Min(0, message = "'page' must be greater than or equal to 0")
    page: Int,
    @RequestParam(defaultValue = "20")
    @Min(1, message = "'size' must be between 1 and 100")
    @Max(100, message = "'size' must be between 1 and 100")
    size: Int,
    @RequestParam(required = false) sort: String?,
  ): ResponseEntity<PagedResponse<Customer>> {
    val customersPage = customerLogic.getCustomersPage(page, size, sort)
    return ResponseEntity.ok(customersPage)
  }

  @PostMapping(path = ["/v1/customers"])
  fun createCustomer(@Valid @RequestBody request: CreateCustomerRequest): ResponseEntity<Void> {
    val customerId = customerLogic.createCustomer(request)

    val location =
      ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(customerId)
        .toUri()

    return ResponseEntity.created(location).build()
  }

  @GetMapping(path = ["/v1/customers/{id}"])
  fun getCustomerById(@PathVariable id: UUID): ResponseEntity<Customer> {
    val customer = customerLogic.getCustomerById(id)
    return ResponseEntity.ok(customer)
  }

  @PatchMapping(path = ["/v1/customers/{id}"])
  fun patchCustomer(
    @PathVariable id: UUID,
    @RequestBody patch: JsonPatch,
  ): ResponseEntity<Customer> {
    val updatedCustomer = customerLogic.patchCustomer(id, patch)
    return ResponseEntity.ok(updatedCustomer)
  }

  @DeleteMapping(path = ["/v1/customers/{id}"])
  fun deleteCustomer(@PathVariable id: UUID): ResponseEntity<Void> {
    customerLogic.deleteCustomer(id)
    return ResponseEntity.noContent().build()
  }
}
