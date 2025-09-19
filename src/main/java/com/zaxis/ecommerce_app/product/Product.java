package com.zaxis.ecommerce_app.product;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.zaxis.ecommerce_app.shared.BigDecimalDeserializer;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nazwa produktu nie może być pusta")
    @Size(min = 3, max = 255, message = "Nazwa musi mieć od 3 do 255 znaków")
    private String name;

    @NotBlank(message = "Opis produktu nie może być pusty")
    @Size(min = 3, max = 255, message = "Nazwa musi mieć od 3 do 255 znaków")
    private String description;

    @NotNull(message = "Cena nie może być pusta")
    @Positive(message = "Cena musi być dodatnia")
    @JsonDeserialize(using = BigDecimalDeserializer.class)
    private BigDecimal price;

    @NotNull(message = "Ilość nie może być pusta")
    @Min(value = 0, message = "Ilość nie może być ujemna")
    private int quantity;
}
