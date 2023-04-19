package com.vineeth.OrderService.service;

import com.vineeth.OrderService.model.OrderRequest;
import com.vineeth.OrderService.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
