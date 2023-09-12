package com.nphase.service;

import com.nphase.entity.Product;
import com.nphase.entity.ProductCategory;
import com.nphase.entity.ShoppingCart;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

public class ShoppingCartService {

    private final int discountAmount;
    private final BigDecimal productDiscount;
    private final BigDecimal categoryDiscount;

    public ShoppingCartService(int discountAmount, BigDecimal productDiscount, BigDecimal categoryDiscount) {
        this.discountAmount = discountAmount;
        this.productDiscount = Objects.requireNonNull(productDiscount);
        this.categoryDiscount = Objects.requireNonNull(categoryDiscount);
    }

    public BigDecimal calculateTotalPrice(ShoppingCart shoppingCart) {
        List<Product> products = shoppingCart.getProducts();
        Map<ProductCategory, Integer> quantityPerCategory = products.stream()
                .collect(groupingBy(Product::getProductCategory, summingInt(Product::getQuantity)));

        return products.stream()
                .map(product -> productPrice(product, quantityPerCategory))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal productPrice(Product product, Map<ProductCategory, Integer> quantityPerCategory) {
        int quantity = product.getQuantity();
        BigDecimal price = product.getPricePerUnit().multiply(BigDecimal.valueOf(quantity));
        BigDecimal discount = calculateDiscount(product, quantityPerCategory);
        boolean hasDiscount = BigDecimal.ZERO.compareTo(discount) < 1;

        return hasDiscount
                ? price.multiply(BigDecimal.ONE.subtract(discount))
                : price;
    }

    private BigDecimal calculateDiscount(Product product, Map<ProductCategory, Integer> quantityPerCategory) {
        BigDecimal discount = BigDecimal.ZERO;

        if (product.getQuantity() > discountAmount) {
            discount = discount.add(productDiscount);
        } else if (quantityPerCategory.getOrDefault(product.getProductCategory(), 0) > discountAmount) {
            discount = discount.add(categoryDiscount);
        }

        return discount;
    }
}
