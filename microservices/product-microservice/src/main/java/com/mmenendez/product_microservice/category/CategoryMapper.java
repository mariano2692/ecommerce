package com.mmenendez.product_microservice.category;

import org.springframework.stereotype.Service;

import com.mmenendez.product_microservice.product.ProductMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryMapper {

    private final ProductMapper productMapper;

    public Category toCategory(CategoryRequest request) {
        return Category.builder()
                .id(request.id())
                .name(request.name())
                .description(request.description())
                .build();
    }

    public CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getProducts().stream()
                        .map(productMapper::toProductResponse)
                        .toList()
        );
    }
}
