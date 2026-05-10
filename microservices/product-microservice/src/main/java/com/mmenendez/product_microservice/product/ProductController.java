package com.mmenendez.product_microservice.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

    private final ProductService service;

    @PostMapping
    @Operation(summary = "Create a new product")
    @ApiResponse(responseCode = "200", description = "Product created, returns product ID")
    @ApiResponse(responseCode = "400", description = "Validation error or category not found")
    public ResponseEntity<Integer> createProduct(@Valid @RequestBody ProductRequest product) {
        return ResponseEntity.ok(service.createProduct(product));
    }

    @GetMapping
    @Operation(summary = "Get all products (paginated and sortable)")
    @ApiResponse(responseCode = "200", description = "Page of products")
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @Parameter(description = "Page number, zero-based") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Field to sort by (id, name, price)") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(service.getProducts(page, size, sortBy, direction));
    }

    @GetMapping("/category/{id}")
    @Operation(summary = "Get products by category ID (paginated and sortable)")
    @ApiResponse(responseCode = "200", description = "Page of products in the given category")
    @ApiResponse(responseCode = "400", description = "Category not found")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategoryId(
            @Parameter(description = "Category ID") @PathVariable Integer id,
            @Parameter(description = "Page number, zero-based") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Field to sort by (id, name, price)") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(service.getProductsByCategoryId(id, page, size, sortBy, direction));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID with all its variants")
    @ApiResponse(responseCode = "200", description = "Product found with variant list")
    @ApiResponse(responseCode = "400", description = "Product not found")
    public ResponseEntity<ProductDetailResponse> getProductById(
            @Parameter(description = "Product ID") @PathVariable Integer id) {
        return ResponseEntity.ok(service.getProductById(id));
    }

    @GetMapping("/variants/{id}")
    @Operation(summary = "Get variant by ID", description = "Used internally by cart and order microservices to fetch price and SKU")
    @ApiResponse(responseCode = "200", description = "Variant found")
    @ApiResponse(responseCode = "400", description = "Variant not found")
    public ResponseEntity<ProductVariantDetailResponse> getVariantById(
            @Parameter(description = "Variant ID") @PathVariable Integer id) {
        return ResponseEntity.ok(service.getVariantById(id));
    }

    @PutMapping
    @Operation(summary = "Update an existing product")
    @ApiResponse(responseCode = "200", description = "Product updated, returns product ID")
    @ApiResponse(responseCode = "400", description = "Product not found or validation error")
    public ResponseEntity<Integer> updateProduct(@Valid @RequestBody ProductRequest product) {
        return ResponseEntity.ok(service.updateProduct(product));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product by ID")
    @ApiResponse(responseCode = "200", description = "Product deleted")
    @ApiResponse(responseCode = "400", description = "Product not found")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Integer id) {
        service.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/purchase")
    @Operation(
        summary = "Deduct stock (internal)",
        description = "Called by Order Microservice during checkout. Applies pessimistic lock to prevent race conditions."
    )
    @ApiResponse(responseCode = "200", description = "Stock deducted successfully")
    @ApiResponse(responseCode = "400", description = "Insufficient stock or variant not found")
    public ResponseEntity<Void> purchaseProduct(@Valid @RequestBody List<ProductQuantityRequest> request) {
        service.purchaseProduct(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restock")
    @Operation(
        summary = "Restore stock (internal)",
        description = "Called by Order Microservice to compensate stock on payment failure or order cancellation"
    )
    @ApiResponse(responseCode = "200", description = "Stock restored successfully")
    @ApiResponse(responseCode = "400", description = "Variant not found")
    public ResponseEntity<Void> updateProductStock(@Valid @RequestBody List<ProductQuantityRequest> request) {
        service.restockProduct(request);
        return ResponseEntity.ok().build();
    }
}
