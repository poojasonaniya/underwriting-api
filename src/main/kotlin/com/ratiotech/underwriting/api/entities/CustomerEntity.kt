package com.ratiotech.underwriting.api.entities

import com.ratiotech.underwriting.api.constants.Schema
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.Date
import java.util.UUID

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(schema = Schema.TENANTS, name = "customers")
class CustomerEntity(
  @Column(name = "customer_name", nullable = false) var name: String?,
  @Column(name = "tax_identifier", nullable = false) var taxIdentifier: String?,
  @Column(name = "created_by", nullable = false, updatable = false) var createdBy: UUID? = null,
  @Column(name = "created_date", nullable = false)
  @CreatedDate
  @Temporal(TemporalType.TIMESTAMP)
  var createdDate: Date? = null,
  @Column(name = "last_modified_by", nullable = false, updatable = false)
  var lastModifiedBy: UUID? = null,
  @Column(name = "last_modified_date", nullable = false)
  @LastModifiedDate
  @Temporal(TemporalType.TIMESTAMP)
  var lastModifiedDate: Date? = null,
  @Id @GeneratedValue @Column(name = "customer_id", nullable = false) var id: UUID? = null,
)
