package com.ratiotech.underwriting.api.controllers

import com.github.fge.jsonpatch.JsonPatch
import com.ratiotech.underwriting.api.controllers.requests.CreateCustomerRequest
import com.ratiotech.underwriting.api.controllers.responses.Customer
import com.ratiotech.underwriting.api.logic.CustomerLogic
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
class CustomerController(private val customerLogic: CustomerLogic) {

  @GetMapping(path = ["/v1/customers"])
  fun c(): ResponseEntity<List<Customer>> {
    val customers = customerLogic.getAllCustomers()
    return ResponseEntity.ok(customers)
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
