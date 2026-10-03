CREATE TABLE IF NOT EXISTS tenants.addresses
(
  address_id uuid NOT NULL,
  address_number VARCHAR(20),
  street VARCHAR(255),
  country VARCHAR(100),
  region VARCHAR(255),
  sub_region VARCHAR(255),
  neighborhood VARCHAR(255),
  municipality VARCHAR(255),
  postal_code VARCHAR(50),
  created_by uuid NOT NULL,
  created_date timestamp NOT NULL,
  last_modified_by uuid,
  last_modified_date timestamp,
  PRIMARY KEY (address_id)
);