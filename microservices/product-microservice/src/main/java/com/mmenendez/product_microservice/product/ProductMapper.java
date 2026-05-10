package com.mmenendez.product_microservice.product;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mmenendez.product_microservice.category.Category;

@Service
public class ProductMapper {

    public ProductResponse toProductResponse(Product product) {
        double priceFrom = product.getVariants().stream()
                .mapToDouble(ProductVariant::getPrice)
                .min()
                .orElse(0.0);
        int totalStock = product.getVariants().stream()
                .mapToInt(ProductVariant::getStock)
                .sum();
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getImageUrl(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getCategory().getDescription(),
                priceFrom,
                totalStock
        );
    }

    public ProductDetailResponse toProductDetailResponse(Product product) {
        List<ProductVariantResponse> variants = product.getVariants().stream()
                .map(this::toVariantResponse)
                .toList();
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getImageUrl(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getCategory().getDescription(),
                variants
        );
    }

    public ProductVariantResponse toVariantResponse(ProductVariant variant) {
        return new ProductVariantResponse(
                variant.getId(),
                variant.getSku(),
                variant.getPrice(),
                variant.getStock(),
                variant.getAttributes()
        );
    }

    public ProductVariantDetailResponse toVariantDetailResponse(ProductVariant variant) {
        return new ProductVariantDetailResponse(
                variant.getId(),
                variant.getSku(),
                variant.getPrice(),
                variant.getStock(),
                variant.getProduct().getId(),
                variant.getProduct().getName(),
                variant.getAttributes()
        );
    }

    public Product toProduct(ProductRequest request) {
        return Product.builder()
                .id(request.id())
                .name(request.name())
                .description(request.description())
                .imageUrl(request.imageUrl())
                .category(Category.builder()
                        .id(request.categoryId())
                        .build())
                .build();
    }

    public ProductVariant toVariant(ProductVariantRequest request) {
        return ProductVariant.builder()
                .id(request.id())
                .sku(request.sku())
                .price(request.price())
                .stock(request.stock())
                .attributes(request.attributes() != null ? request.attributes() : new java.util.HashMap<>())
                .build();
    }
}
