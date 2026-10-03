package com.ratiotech.underwriting.api.logic

import com.github.fge.jsonpatch.JsonPatch
import com.ratiotech.underwriting.api.controllers.requests.CreateCustomerRequest
import com.ratiotech.underwriting.api.controllers.requests.UpdateCustomerModel
import com.ratiotech.underwriting.api.controllers.responses.Customer
import com.ratiotech.underwriting.api.controllers.responses.PagedResponse
import com.ratiotech.underwriting.api.entities.CustomerEntity
import com.ratiotech.underwriting.api.logic.translators.CustomerTranslator
import com.ratiotech.underwriting.api.repositories.CustomerRepository
import com.ratiotech.underwriting.api.shared.constants.Constants.SYSTEM_IDENTITY
import com.ratiotech.underwriting.api.shared.exceptions.BadRequestException
import com.ratiotech.underwriting.api.shared.exceptions.NotFoundException
import com.ratiotech.underwriting.api.shared.logic.JsonPatchManager
import java.util.UUID
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@Component
class CustomerLogic
(
  private val customerRepository: CustomerRepository,
  private val customerTranslator: CustomerTranslator,
  private val jsonPatchManager: JsonPatchManager,
) {

  /**
   * Creates a new customer from the provided request.
   *
   * @param request the create customer request containing customer details
   * @return the UUID of the created customer
   * @throws BadRequestException if a customer with the same tax identifier already exists
   */
  @Transactional
  fun createCustomer(request: CreateCustomerRequest): UUID {
    // Check if customer already exists by tax identifier
    if (customerRepository.existsByTaxIdentifier(request.taxIdentifier)) {
      throw BadRequestException(
        "Customer with tax identifier '${request.taxIdentifier}' already exists"
      )
    }

    // Create new customer entity
    val customerEntity =
      CustomerEntity(
        name = request.name,
        taxIdentifier = request.taxIdentifier,
        createdBy = SYSTEM_IDENTITY,
        lastModifiedBy = SYSTEM_IDENTITY,
      )

    // Save and return the customer UUID
    val savedCustomer = customerRepository.save(customerEntity)
    return savedCustomer.id!!
  }

  /**
   * Retrieves all customers.
   *
   * @return a list of all Customer response models
   */
  @Transactional(readOnly = true)
  fun getAllCustomers(): List<Customer> =
    customerRepository.findAll().map { customerTranslator.toModel(it) }

  /**
   * Retrieves a customer by their ID.
   *
   * @param customerId the UUID of the customer to retrieve
   * @return the Customer response model
   * @throws NotFoundException if no customer is found with the given ID
   */
  @Transactional(readOnly = true)
  fun getCustomerById(customerId: UUID): Customer {
    val customerEntity =
      customerRepository.findById(customerId).orElseThrow {
        NotFoundException("Customer not found with id: $customerId")
      }
    return customerTranslator.toModel(customerEntity)
  }

  @Transactional(readOnly = true)
  fun getCustomersPage(page: Int, size: Int, sort: String?): PagedResponse<Customer> {
    val pageable = PageRequest.of(page, size, parseSort(sort))
    val customersPage = customerRepository.findAll(pageable).map { customerTranslator.toModel(it) }
    return PagedResponse.from(customersPage)
  }

  /**
   * Patches a customer using JSON Patch operations.
   *
   * @param customerId the UUID of the customer to patch
   * @param patch the JSON patch operations to apply
   * @return the updated Customer response model
   * @throws NotFoundException if no customer is found with the given ID
   */
  @Transactional
  fun patchCustomer(customerId: UUID, patch: JsonPatch): Customer {
    // 1. Find the customer entity
    val customerEntity =
      customerRepository.findById(customerId).orElseThrow {
        NotFoundException("Customer not found with id: $customerId")
      }

    // 2. Convert entity to update model
    val updateModel = customerTranslator.toUpdateModel(customerEntity)

    // 3. Apply JSON patch to the update model
    val patchedUpdateModel =
      jsonPatchManager.applyPatch(patch, updateModel, UpdateCustomerModel::class.java)

    // 4. Update the entity with the patched values
    customerTranslator.updateEntity(customerEntity, patchedUpdateModel)

    // 5. Save the updated entity
    val savedEntity = customerRepository.save(customerEntity)

    // 6. Return the updated customer as response model
    return customerTranslator.toModel(savedEntity)
  }

  /**
   * Deletes a customer by their ID.
   *
   * @param customerId the UUID of the customer to delete
   * @throws NotFoundException if no customer is found with the given ID
   */
  @Transactional
  fun deleteCustomer(customerId: UUID) {
    // Check if customer exists before attempting to delete
    if (!customerRepository.existsById(customerId)) {
      throw NotFoundException("Customer not found with id: $customerId")
    }

    // Delete the customer
    customerRepository.deleteById(customerId)
  }

    /**
   * Parses a sort expression such as `name,desc` into a [Sort].
   *
   * @param sort the sort expression, or null/blank for the default sort
   * @return the [Sort] to apply, always ending with `id` ascending as a tie-breaker
   * @throws BadRequestException if the field is not sortable or the direction is invalid
   */
  private fun parseSort(sort: String?): Sort {
    if (sort.isNullOrBlank()) {
      return Sort.by(Sort.Order.asc(DEFAULT_SORT_FIELD), Sort.Order.asc(ID_FIELD))
    }

    val parts = sort.split(",").map { it.trim() }
    if (parts.size > 2) {
      throw BadRequestException("Invalid sort '$sort'. Expected format: field,direction")
    }

    val field = parts[0]
    if (field !in SORTABLE_FIELDS) {
      throw BadRequestException(
        "Invalid sort field '$field'. Allowed fields: ${SORTABLE_FIELDS.joinToString()}"
      )
    }

    val direction =
      parts.getOrNull(1)?.let {
        Sort.Direction.fromOptionalString(it).orElseThrow {
          BadRequestException("Invalid sort direction '$it'. Allowed values: asc, desc")
        }
      } ?: Sort.Direction.ASC

    return Sort.by(Sort.Order(direction, field), Sort.Order.asc(ID_FIELD))
  }

  companion object {
    private const val ID_FIELD = "id"
    private const val DEFAULT_SORT_FIELD = "createdDate"
    private val SORTABLE_FIELDS =
      setOf("name", "taxIdentifier", "createdDate", "lastModifiedDate")
  }
}
