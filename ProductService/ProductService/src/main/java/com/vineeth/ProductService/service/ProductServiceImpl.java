package com.vineeth.ProductService.service;

import com.vineeth.ProductService.entity.Product;
import com.vineeth.ProductService.exception.ProductServiceCustomException;
import com.vineeth.ProductService.model.ProductRequest;
import com.vineeth.ProductService.model.ProductResponse;
import com.vineeth.ProductService.repository.ProductRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public long addProduct(ProductRequest productRequest) {
        log.info("Adding Product...");

        Product product = Product.builder()
                .productName(productRequest.getName())
                .quantity(productRequest.getQuantity())
                .price(productRequest.getPrice())
                .build();
        productRepository.save(product);
        log.info("Product Created");
        return product.getProductId();
    }

    @Override
    public ProductResponse getProductById(Long productId) {
        log.info("Get the product for productId: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductServiceCustomException("Product Id not found", "PRODUCT_NOT_FOUND"));
        ProductResponse productResponse = new ProductResponse();
        BeanUtils.copyProperties(product, productResponse);
        log.info("Product with id {} returned", productId);
        return productResponse;
    }

    @Override
    public List<ProductResponse> getProducts() {
        log.info("Getting all products");
        List<Product> products = productRepository.findAll();
        ArrayList<ProductResponse> productResponses = new ArrayList<ProductResponse>();
        for(Product product:products){
            ProductResponse productResponse = new ProductResponse();
            BeanUtils.copyProperties(product, productResponse);
            productResponses.add(productResponse);
        }
        log.info("Returned all products");
        return productResponses;
    }

    @Override
    public void reduceQuantity(long productId, long quantity) {
        log.info("Reduce Quantity {} for ID {}", quantity, productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductServiceCustomException(
                        "Product with given ID is not found",
                        "PRODUCT_NOT_FOUND"
                ));
        if(product.getQuantity() < quantity){
            throw new ProductServiceCustomException(
                    "Product has insufficient quantity",
                    "INSUFFICIENT_QUANTITY"
            );
        }
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
        log.info("Product quantity updated successfully");
    }
}
