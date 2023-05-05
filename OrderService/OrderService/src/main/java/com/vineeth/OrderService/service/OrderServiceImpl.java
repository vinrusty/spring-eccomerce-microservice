package com.vineeth.OrderService.service;

import com.vineeth.OrderService.entity.Order;
import com.vineeth.OrderService.exception.CustomException;
import com.vineeth.OrderService.external.client.PaymentService;
import com.vineeth.OrderService.external.client.ProductService;
import com.vineeth.OrderService.external.request.PaymentRequest;
import com.vineeth.OrderService.external.response.PaymentResponse;
import com.vineeth.OrderService.model.OrderRequest;
import com.vineeth.OrderService.model.OrderResponse;
import com.vineeth.OrderService.model.ProductResponse;
import com.vineeth.OrderService.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService{
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestTemplate restTemplate;
    @Value("${microservices.product}")
    private String productServiceUrl;
    @Value("${microservices.payment}")
    private String paymentServiceUrl;

    @Override
    public long placeOrder(OrderRequest orderRequest) {
        log.info("Placing Order request: {}", orderRequest);
        log.info("Creating order with status CREATED");
        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .build();

        order = orderRepository.save(order);

        log.info("Calling payment service to complete the payment");

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();
        String orderStatus = null;
        try{
            paymentService.doPayment(paymentRequest);
            log.info("Payment done Successfully. Changing the order status to placed");
            productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());
            orderStatus = "PLACED";
        }
        catch(Exception e){
            log.info("Payment unsuccessful. Changing the order status to PAYMENT_FAILED");
            orderStatus = "PAYMENT_FAILED";
        }
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
        log.info("Order placed successfully with Order id: {}", order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Get order details for order Id: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found for the Order ID "+orderId,
                        "NOT_FOUND", 404));
        log.info("Invoking Product service to fetch the product details for Order ID {}", order.getId());
        ProductResponse productResponse = restTemplate.getForObject(
                productServiceUrl+order.getProductId(),
                ProductResponse.class
        );
        log.info("Getting payment information from the payment service");
        PaymentResponse paymentResponse = restTemplate.getForObject(
                paymentServiceUrl+"order/"+order.getId(),
                PaymentResponse.class
        );
        OrderResponse.ProductDetails productDetails = OrderResponse.ProductDetails.builder()
                .productName(productResponse.getProductName())
                .productId(productResponse.getProductId())
                .price(productResponse.getPrice())
                .build();
        OrderResponse.PaymentDetails paymentDetails = OrderResponse.PaymentDetails.builder()
                .paymentId(paymentResponse.getPaymentId())
                .paymentDate(paymentResponse.getPaymentDate())
                .paymentMode(paymentResponse.getPaymentMode())
                .amount(paymentResponse.getAmount())
                .status(paymentResponse.getStatus())
                .build();
        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .amount(order.getAmount())
                .orderDate(order.getOrderDate())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();
        return orderResponse;
    }
}
