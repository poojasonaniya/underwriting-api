BEGIN TRANSACTION;

CREATE TABLE IF NOT EXISTS tenants.users
(
  user_id uuid NOT NULL,
  email varchar(255) NOT NULL,
  first_name varchar(255),
  last_name varchar(255),
  title varchar(255),
  phone_number varchar(255),
  created_by uuid NOT NULL,
  created_date timestamp NOT NULL,
  last_modified_by uuid,
  last_modified_date timestamp,
  PRIMARY KEY (user_id)
);

COMMIT;
