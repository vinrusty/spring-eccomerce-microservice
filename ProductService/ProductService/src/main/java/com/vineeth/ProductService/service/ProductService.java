package com.vineeth.ProductService.service;

import com.vineeth.ProductService.model.ProductRequest;
import com.vineeth.ProductService.model.ProductResponse;

import java.util.List;

public interface ProductService {
    long addProduct(ProductRequest productRequest);

    ProductResponse getProductById(Long productId);

    List<ProductResponse> getProducts();

    void reduceQuantity(long productId, long quantity);
}
