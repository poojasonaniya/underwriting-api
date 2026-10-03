package com.ratiotech.underwriting.api.logic.translators

import com.ratiotech.underwriting.api.controllers.requests.UpdateCustomerModel
import com.ratiotech.underwriting.api.controllers.responses.Customer
import com.ratiotech.underwriting.api.entities.CustomerEntity
import org.springframework.stereotype.Component

@Component
class CustomerTranslator {

  /**
   * Converts a CustomerEntity to a Customer response model.
   *
   * @param customerEntity the CustomerEntity to convert
   * @return the Customer response model
   */
  fun toModel(customerEntity: CustomerEntity): Customer =
    Customer(
      id = customerEntity.id,
      name = customerEntity.name,
      taxIdentifier = customerEntity.taxIdentifier,
      createdDate = customerEntity.createdDate,
      createdBy = customerEntity.createdBy,
      lastModifiedBy = customerEntity.lastModifiedBy,
      lastModifiedDate = customerEntity.lastModifiedDate,
    )

  /**
   * Converts a CustomerEntity to an UpdateCustomerModel.
   *
   * @param customerEntity the CustomerEntity to convert
   * @return the UpdateCustomerModel
   */
  fun toUpdateModel(customerEntity: CustomerEntity): UpdateCustomerModel =
    UpdateCustomerModel(
      name = customerEntity.name,
      taxIdentifier = customerEntity.taxIdentifier,
    )

  /**
   * Updates a CustomerEntity with values from an UpdateCustomerModel.
   *
   * @param customerEntity the CustomerEntity to update
   * @param updateModel the UpdateCustomerModel containing new values
   */
  fun updateEntity(customerEntity: CustomerEntity, updateModel: UpdateCustomerModel) {
    updateModel.name?.let { customerEntity.name = it }
    updateModel.taxIdentifier?.let { customerEntity.taxIdentifier = it }
  }
}
