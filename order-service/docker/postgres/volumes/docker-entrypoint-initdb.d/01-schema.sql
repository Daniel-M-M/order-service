-- postgres.orders definition

-- Drop table

-- DROP TABLE postgres.orders;
--
-- CREATE TABLE postgres.orders (
--      order_id uuid DEFAULT gen_random_uuid() NOT NULL,
--      name varchar(100) NULL,
--      email varchar(250) NULL,
--      created_on timestamptz(6) NOT NULL,
--      cognome varchar(100) NULL,
--      data_order date DEFAULT CURRENT_DATE NULL
-- );