CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    customer_name TEXT NOT NULL,
    product_name TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    total_price NUMERIC(10,2) NOT NULL,
    payment_status TEXT NOT NULL,
    delivery_status TEXT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    module_code TEXT NOT NULL CHECK (module_code IN ('finance', 'inventory', 'hrm', 'sales')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
