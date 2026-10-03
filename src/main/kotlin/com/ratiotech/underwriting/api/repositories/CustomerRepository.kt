package com.ratiotech.underwriting.api.repositories

import com.ratiotech.underwriting.api.entities.CustomerEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository : JpaRepository<CustomerEntity, UUID> {
  fun existsByTaxIdentifier(taxIdentifier: String): Boolean
}
