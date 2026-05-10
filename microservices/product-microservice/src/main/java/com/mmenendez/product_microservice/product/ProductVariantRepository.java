package com.mmenendez.product_microservice.product;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    // Lock pesimista: bloquea la fila hasta que la transacción termine,
    // evitando que dos checkouts simultáneos lean el mismo stock y ambos pasen la validación
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariant v WHERE v.id = :id")
    Optional<ProductVariant> findByIdWithLock(Integer id);
}
