package com.nathanaeldorsey.stormmarket.common.exception;

import com.nathanaeldorsey.stormmarket.cart.CartError;
import com.nathanaeldorsey.stormmarket.order.OrderError;
import com.nathanaeldorsey.stormmarket.payment.PaymentError;
import com.nathanaeldorsey.stormmarket.product.StockError;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ResultHttpMapperTest {

    @Autowired
    private ResultHttpMapper resultHttpMapper;

    @Test
    void toResponseOkWithValueReturns200() {
        Result<String, CartError> result = Result.ok("success");
        setRequestContext("/test/path");

        ResponseEntity<?> response = resultHttpMapper.toResponse(result);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("success");
    }

    @Test
    void toResponseOkWithNullReturns204() {
        Result<Void, CartError> result = Result.ok(null);
        setRequestContext("/test/path");

        ResponseEntity<?> response = resultHttpMapper.toResponse(result);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void toResponseErrCartErrorInactiveProductReturns400() {
        Result<Void, CartError> result = Result.err(CartError.INACTIVE_PRODUCT);
        setRequestContext("/v1/cart/items");

        ResponseEntity<?> response = resultHttpMapper.toResponse(result);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void toResponseErrCartErrorInsufficientStockReturns409() {
        Result<Void, CartError> result = Result.err(CartError.INSUFFICIENT_STOCK);
        setRequestContext("/v1/cart/items");

        ResponseEntity<?> response = resultHttpMapper.toResponse(result);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void toResponseErrCartErrorCartNotFoundReturns404() {
        Result<Void, CartError> result = Result.err(CartError.CART_NOT_FOUND);
        setRequestContext("/v1/cart");

        ResponseEntity<?> response = resultHttpMapper.toResponse(result);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void toResponseErrOrderErrorInvalidStatusTransitionReturns409() {
        Result<Void, OrderError> result = Result.err(OrderError.INVALID_STATUS_TRANSITION);
        setRequestContext("/v1/admin/orders/1/status");

        ResponseEntity<?> response = resultHttpMapper.toResponse(result);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void toResponseErrOrderErrorOrderNotFoundReturns404() {
        Result<Void, OrderError> result = Result.err(OrderError.ORDER_NOT_FOUND);
        setRequestContext("/v1/orders/999");

        ResponseEntity<?> response = resultHttpMapper.toResponse(result);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void toResponseErrPaymentErrorOwnershipMismatchReturns403() {
        Result<Void, PaymentError> result = Result.err(PaymentError.OWNERSHIP_MISMATCH);
        setRequestContext("/v1/payments/pi_test/capture");

        ResponseEntity<?> response = resultHttpMapper.toResponse(result);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void toResponseErrPaymentErrorPaymentNotFoundReturns404() {
        Result<Void, PaymentError> result = Result.err(PaymentError.PAYMENT_NOT_FOUND);
        setRequestContext("/v1/payments/pi_test");

        ResponseEntity<?> response = resultHttpMapper.toResponse(result);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void toResponseErrStockErrorInsufficientStockReturns409() {
        Result<Integer, StockError> result = Result.err(StockError.INSUFFICIENT_STOCK);
        setRequestContext("/internal/stock");

        ResponseEntity<?> response = resultHttpMapper.toResponse(result);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void toResponseErrUnknownErrorReturns500() {
        Result<Void, RuntimeException> result = Result.err(new RuntimeException("Unknown error"));
        setRequestContext("/test/path");

        ResponseEntity<?> response = resultHttpMapper.toResponse(result);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }

    private void setRequestContext(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}