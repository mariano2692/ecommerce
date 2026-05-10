package com.mmenendez.product_microservice.category;

import java.util.List;

import com.mmenendez.product_microservice.product.Product;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name="categories")
public class Category {
    @Id
    // IDENTITY lets PostgreSQL generate the id via SERIAL, which aligns with
    // the SERIAL PRIMARY KEY defined in the Flyway migration. Without this,
    // Hibernate 6 defaults to SEQUENCE strategy and looks for a named sequence
    // that Flyway would also need to create explicitly.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String description;
    @OneToMany(mappedBy = "category" , fetch = FetchType.LAZY)
    private List<Product> products;
}
