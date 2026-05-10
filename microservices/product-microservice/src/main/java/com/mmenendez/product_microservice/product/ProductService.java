package com.mmenendez.product_microservice.product;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.mmenendez.product_microservice.category.CategoryService;
import com.mmenendez.product_microservice.exceptions.ProductException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final ProductVariantRepository variantRepository;
    private final CategoryService categoryService;
    private final ProductMapper mapper;

    public Page<ProductResponse> getProducts(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return repository.findAll(PageRequest.of(page, size, sort))
                .map(mapper::toProductResponse);
    }

    public ProductDetailResponse getProductById(Integer id) {
        if (id == null) {
            throw new ProductException("Product ID cannot be null");
        }
        return repository.findById(id)
                .map(mapper::toProductDetailResponse)
                .orElseThrow(() -> new ProductException("Product with ID %s not found".formatted(id)));
    }

    public ProductVariantDetailResponse getVariantById(Integer variantId) {
        if (variantId == null) {
            throw new ProductException("Variant ID cannot be null");
        }
        return variantRepository.findById(variantId)
                .map(mapper::toVariantDetailResponse)
                .orElseThrow(() -> new ProductException("Variant with ID %s not found".formatted(variantId)));
    }

    public Page<ProductResponse> getProductsByCategoryId(Integer categoryId, int page, int size, String sortBy, String direction) {
        if (categoryId == null) {
            throw new ProductException("Category ID cannot be null");
        }
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return repository.findByCategoryId(categoryId, PageRequest.of(page, size, sort))
                .map(mapper::toProductResponse);
    }

    @Transactional
    public Integer createProduct(ProductRequest request) {
        if (categoryService.getCategoryById(request.categoryId()) == null) {
            throw new ProductException("Category with ID %s not found".formatted(request.categoryId()));
        }
        Product product = mapper.toProduct(request);
        request.variants().forEach(variantRequest -> {
            ProductVariant variant = mapper.toVariant(variantRequest);
            variant.setProduct(product);
            product.getVariants().add(variant);
        });
        return repository.save(product).getId(); // CascadeType.ALL persiste las variantes
    }

    @Transactional
    public Integer updateProduct(ProductRequest request) {
        if (request.id() == null) {
            throw new ProductException("Product ID cannot be null");
        }
        if (categoryService.getCategoryById(request.categoryId()) == null) {
            throw new ProductException("Category with ID %s not found".formatted(request.categoryId()));
        }
        Product existingProduct = repository.findById(request.id())
                .orElseThrow(() -> new ProductException("Product with ID %s not found".formatted(request.id())));

        existingProduct.setName(request.name());
        existingProduct.setDescription(request.description());
        existingProduct.setImageUrl(request.imageUrl());
        existingProduct.setCategory(com.mmenendez.product_microservice.category.Category.builder().id(request.categoryId()).build());

        // IDs de variantes que vienen en el request (solo las que ya existen)
        Set<Integer> incomingIds = request.variants().stream()
                .map(ProductVariantRequest::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Eliminar variantes que no están en el request (ojo: orphanRemoval = true hace el DELETE)
        existingProduct.getVariants().removeIf(v -> !incomingIds.contains(v.getId()));

        for (ProductVariantRequest variantRequest : request.variants()) {
            if (variantRequest.id() != null) {
                // Actualizar variante existente sin cambiar su ID
                existingProduct.getVariants().stream()
                        .filter(v -> v.getId().equals(variantRequest.id()))
                        .findFirst()
                        .ifPresent(v -> {
                            v.setSku(variantRequest.sku());
                            v.setPrice(variantRequest.price());
                            if (variantRequest.stock() != null) v.setStock(variantRequest.stock());
                            if (variantRequest.attributes() != null) v.setAttributes(variantRequest.attributes());
                        });
            } else {
                // Nueva variante sin ID
                ProductVariant newVariant = mapper.toVariant(variantRequest);
                newVariant.setProduct(existingProduct);
                existingProduct.getVariants().add(newVariant);
            }
        }

        repository.save(existingProduct);
        return existingProduct.getId();
    }

    public void deleteProduct(Integer id) {
        if (id == null) {
            throw new ProductException("Product ID cannot be null");
        }
        if (!repository.existsById(id)) {
            throw new ProductException("Product with ID %s not found".formatted(id));
        }
        repository.deleteById(id);
    }

    @Transactional
    public void purchaseProduct(List<ProductQuantityRequest> request) {
        for (ProductQuantityRequest item : request) {
            ProductVariant variant = variantRepository.findByIdWithLock(item.variantId())
                    .orElseThrow(() -> new ProductException("Variant with ID %s not found".formatted(item.variantId())));

            if (item.quantity() < 0) {
                throw new ProductException("Quantity cannot be negative for variant ID %s".formatted(item.variantId()));
            }
            if (variant.getStock() < item.quantity()) {
                throw new ProductException("Insufficient stock for variant ID %s".formatted(item.variantId()));
            }

            variant.setStock(variant.getStock() - item.quantity());
            variantRepository.save(variant);
        }
    }

    @Transactional
    public void restockProduct(List<ProductQuantityRequest> request) {
        for (ProductQuantityRequest item : request) {
            ProductVariant variant = variantRepository.findById(item.variantId())
                    .orElseThrow(() -> new ProductException("Variant with ID %s not found".formatted(item.variantId())));

            if (item.quantity() < 0) {
                throw new ProductException("Quantity cannot be negative for variant ID %s".formatted(item.variantId()));
            }

            variant.setStock(variant.getStock() + item.quantity());
            variantRepository.save(variant);
        }
    }
}
