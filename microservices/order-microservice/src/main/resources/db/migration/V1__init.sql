-- Initial schema for order-microservice.
-- Managed by Flyway: do not modify this file after it has been applied to any environment.
-- To change the schema, create a new migration file: V2__<description>.sql

-- orders tracks the lifecycle of a purchase from creation to delivery.
-- status is stored as VARCHAR rather than a DB enum so that adding new OrderStatus
-- values in Java requires only a new Flyway migration, not an ALTER TYPE statement.
-- created_at is set by the @PrePersist hook in the Order entity, not by a DB default,
-- so it reflects application time rather than database server time.
CREATE TABLE orders (
    id           BIGSERIAL PRIMARY KEY,
    customer_id  VARCHAR(255),
    status       VARCHAR(50) NOT NULL,
    created_at   TIMESTAMP,
    total_amount FLOAT8
);

-- order_items are the individual line items of an order. They store a snapshot of the
-- product data at the time of purchase (variant_sku, product_name, unit_price) so that
-- historical orders remain accurate even if the product catalog changes later.
-- Deletes cascade from order: removing an order removes all its items.
CREATE TABLE order_items (
    id           BIGSERIAL PRIMARY KEY,
    variant_id   INTEGER,
    variant_sku  VARCHAR(255),
    product_name VARCHAR(255),
    quantity     INTEGER,
    unit_price   FLOAT8,
    order_id     BIGINT NOT NULL REFERENCES orders(id)
);

-- Index on customer_id to speed up "my orders" queries, which filter all orders
-- by the authenticated customer. Without this index the query scans the full table.
CREATE INDEX idx_orders_customer_id ON orders(customer_id);

-- Index on order_id to speed up loading all items for a given order,
-- which happens every time an order detail page is rendered.
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
