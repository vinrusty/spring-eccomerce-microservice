package com.vineeth.PaymentService.service;

import com.vineeth.PaymentService.model.PaymentRequest;
import com.vineeth.PaymentService.model.PaymentResponse;

public interface PaymentService {
    Long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(String orderId);
}
