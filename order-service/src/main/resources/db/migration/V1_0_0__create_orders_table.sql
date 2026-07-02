CREATE TABLE IF NOT EXISTS orders (
    order_id uuid DEFAULT gen_random_uuid(),
    name varchar(100),
    email varchar(250),
    created_on timestamptz(6) NOT NULL,
    CONSTRAINT orders_pkey PRIMARY KEY (order_id)
    );