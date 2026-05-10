package com.mmenendez.product_microservice.category;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Product category management endpoints")
public class CategoryController {

    private final CategoryService service;

    @GetMapping
    @Operation(summary = "Get all categories")
    @ApiResponse(responseCode = "200", description = "List of all categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(service.getAllCategories());
    }

    @PostMapping
    @Operation(summary = "Create a new category")
    @ApiResponse(responseCode = "200", description = "Category created, returns category ID")
    @ApiResponse(responseCode = "400", description = "Validation error in request fields")
    public ResponseEntity<Integer> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(service.createCategory(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    @ApiResponse(responseCode = "200", description = "Category found")
    @ApiResponse(responseCode = "400", description = "Category not found")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable("id") Integer id) {
        return ResponseEntity.ok(service.getCategoryById(id));
    }

    @PutMapping
    @Operation(summary = "Update an existing category")
    @ApiResponse(responseCode = "202", description = "Category updated")
    @ApiResponse(responseCode = "400", description = "Category not found or validation error")
    public ResponseEntity<Void> updateCategory(@Valid @RequestBody CategoryRequest request) {
        service.updateCategory(request);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category by ID")
    @ApiResponse(responseCode = "202", description = "Category deleted")
    @ApiResponse(responseCode = "400", description = "Category not found")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable("id") Integer id) throws Exception {
        service.deleteCategory(id);
        return ResponseEntity.accepted().build();
    }
}
