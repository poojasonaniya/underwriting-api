package com.ratiotech.underwriting.api.entities;

import com.ratiotech.underwriting.api.constants.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder(toBuilder = true)
@Table(schema = Schema.TENANTS, name = "users")
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {
  @Id
  @GeneratedValue
  @Column(name = "user_id")
  private UUID id;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "title")
  private String title;

  @Column(name = "phone_number")
  private String phoneNumber;

  @Column(name = "created_by", nullable = false, updatable = false)
  protected UUID createdBy;

  @Column(name = "created_date", nullable = false, updatable = false)
  @CreatedDate
  @Temporal(TemporalType.TIMESTAMP)
  protected Date createdDate;

  @Column(name = "last_modified_by")
  protected UUID lastModifiedBy;

  @Column(name = "last_modified_date")
  @LastModifiedDate
  @Temporal(TemporalType.TIMESTAMP)
  protected Date lastModifiedDate;

  public String getFullName() {
    return Stream.of(firstName, lastName)
        .filter(StringUtils::hasText)
        .collect(Collectors.joining(" "));
  }
}
