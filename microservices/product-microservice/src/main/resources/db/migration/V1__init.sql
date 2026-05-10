-- Initial schema for product-microservice.
-- Managed by Flyway: do not modify this file after it has been applied to any environment.
-- To change the schema, create a new migration file: V2__<description>.sql

-- categories is the root of the product hierarchy. Products belong to exactly one category.
-- A category can exist without products (e.g. newly created, not yet populated).
CREATE TABLE categories (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(255),
    description VARCHAR(255)
);

-- products belong to a category. Deleting a category is intentionally blocked
-- by the FK constraint if products still reference it, preventing orphaned products.
CREATE TABLE products (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(255),
    description VARCHAR(255),
    image_url   VARCHAR(255),
    category_id INTEGER REFERENCES categories(id)
);

-- product_variants hold the purchasable units of a product (e.g. size S, color red).
-- stock defaults to 0 rather than NULL so that stock checks never require null handling.
-- Deletes cascade from product: removing a product removes all its variants.
-- The pessimistic lock in ProductVariantRepository targets this table during checkout
-- to prevent overselling when concurrent orders race for the last unit.
CREATE TABLE product_variants (
    id         SERIAL PRIMARY KEY,
    sku        VARCHAR(255),
    price      FLOAT8,
    stock      INTEGER NOT NULL DEFAULT 0,
    product_id INTEGER NOT NULL REFERENCES products(id)
);

-- variant_attributes stores arbitrary key-value pairs per variant (e.g. color=red, size=S).
-- Modeled as a @ElementCollection in JPA, which maps to this separate table.
-- The composite PK (variant_id, attr_key) enforces that each attribute key is unique per variant.
CREATE TABLE variant_attributes (
    variant_id INTEGER      NOT NULL REFERENCES product_variants(id),
    attr_key   VARCHAR(255) NOT NULL,
    attr_value VARCHAR(255),
    PRIMARY KEY (variant_id, attr_key)
);

-- Index on category_id to speed up queries that filter products by category,
-- which is the most common product listing pattern in the frontend.
CREATE INDEX idx_products_category_id ON products(category_id);

-- Index on product_id to speed up loading all variants for a given product,
-- which happens on every product detail page and during checkout stock validation.
CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);
