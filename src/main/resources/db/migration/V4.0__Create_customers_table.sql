BEGIN TRANSACTION;

CREATE TABLE tenants.customers (
  customer_id UUID NOT NULL,
  address_id UUID,
  customer_name VARCHAR(255) NOT NULL,
  tax_identifier VARCHAR(255) NOT NULL,
  created_by UUID NOT NULL,
  created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  last_modified_by UUID,
  last_modified_date TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT pk_customers PRIMARY KEY (customer_id)
);

ALTER TABLE tenants.customers ADD CONSTRAINT FK_CUSTOMERS_ON_ADDRESS FOREIGN KEY (address_id) REFERENCES tenants.addresses (address_id);

COMMIT;