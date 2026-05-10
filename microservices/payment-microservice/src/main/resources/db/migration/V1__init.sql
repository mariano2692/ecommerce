-- Initial schema for payment-microservice.
-- Managed by Flyway: do not modify this file after it has been applied to any environment.
-- To change the schema, create a new migration file: V2__<description>.sql

-- payments records every payment attempt for an order, including failures.
-- One order can have multiple payment rows if the first attempt fails and the customer retries.
-- order_id and customer_id are plain columns rather than FKs because payments lives in a
-- separate database from orders; cross-database FK constraints are not supported in PostgreSQL.
-- payment_method and status are VARCHAR rather than DB enums for the same reason as
-- order status: adding new enum values in Java should not require ALTER TYPE migrations.
CREATE TABLE payments (
    id             BIGSERIAL PRIMARY KEY,
    order_id       BIGINT,
    customer_id    VARCHAR(255),
    amount         FLOAT8,
    payment_method VARCHAR(50),
    status         VARCHAR(50) NOT NULL,
    created_at     TIMESTAMP
);

-- Index on order_id to quickly find the payment(s) for a given order.
-- Used by the order-microservice Feign client when confirming that payment succeeded.
CREATE INDEX idx_payments_order_id ON payments(order_id);

-- Index on customer_id to support future "payment history" queries per customer
-- without a full table scan as the payments table grows over time.
CREATE INDEX idx_payments_customer_id ON payments(customer_id);
