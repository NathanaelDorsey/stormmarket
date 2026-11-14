package com.nathanaeldorsey.stormmarket.common.exception;

import com.nathanaeldorsey.stormmarket.cart.CartError;
import com.nathanaeldorsey.stormmarket.order.OrderError;
import com.nathanaeldorsey.stormmarket.payment.PaymentError;
import com.nathanaeldorsey.stormmarket.product.StockError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Maps Result<T, E> to HTTP responses with appropriate status codes and ApiErrorResponse bodies.
 * Used by controllers to convert domain Result objects to HTTP responses.
 */
@Component
public class ResultHttpMapper {

    /**
     * Converts a Result to an HTTP ResponseEntity.
     *
     * @param result the Result to convert
     * @param <T>    the success value type
     * @param <E>    the error type
     * @return ResponseEntity with appropriate status code and body
     */
    public <T, E extends Throwable> ResponseEntity<?> toResponse(Result<T, E> result) {
        HttpServletRequest request = getCurrentRequest();
        String path = request != null ? request.getRequestURI() : "unknown";

        if (result.isOk()) {
            T value = result.getValue();
            if (value == null) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(value);
        }

        E error = result.getError();
        HttpStatus status = mapErrorToStatus(error);
        ApiErrorResponse body = buildErrorResponse(status, error.getMessage(), path);
        return ResponseEntity.status(status).body(body);
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    private HttpStatus mapErrorToStatus(Throwable error) {
        // CartError mapping
        if (error instanceof CartError cartError) {
            return mapCartError(cartError);
        }

        // OrderError mapping
        if (error instanceof OrderError orderError) {
            return mapOrderError(orderError);
        }

        // PaymentError mapping
        if (error instanceof PaymentError paymentError) {
            return mapPaymentError(paymentError);
        }

        // StockError mapping (internal use)
        if (error instanceof StockError stockError) {
            return mapStockError(stockError);
        }

        // ResourceNotFoundException -> 404
        if (error instanceof com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }

        // IllegalArgumentException -> 400
        if (error instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }

        // IllegalStateException -> 409
        if (error instanceof IllegalStateException) {
            return HttpStatus.CONFLICT;
        }

        // Default -> 500
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private HttpStatus mapCartError(CartError error) {
        if (error == CartError.INVALID_QUANTITY) {
            return HttpStatus.BAD_REQUEST;
        }
        if (error == CartError.INSUFFICIENT_STOCK
                || error == CartError.CART_FULL) {
            return HttpStatus.CONFLICT;
        }
        if (error == CartError.CART_NOT_FOUND
                || error == CartError.PRODUCT_NOT_FOUND
                || error == CartError.PRODUCT_NOT_IN_CART) {
            return HttpStatus.NOT_FOUND;
        }
        if (error == CartError.USER_NOT_FOUND) {
            return HttpStatus.NOT_FOUND;
        }
        if (error == CartError.INACTIVE_PRODUCT) {
            return HttpStatus.BAD_REQUEST;
        }
        if (error == CartError.COUPON_INVALID
                || error == CartError.COUPON_EXPIRED
                || error == CartError.COUPON_USAGE_LIMIT_REACHED
                || error == CartError.COUPON_INACTIVE) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private HttpStatus mapOrderError(OrderError error) {
        if (error == OrderError.INVALID_STATUS_TRANSITION
                || error == OrderError.PAYMENT_NOT_CAPTURED
                || error == OrderError.INSUFFICIENT_STOCK) {
            return HttpStatus.CONFLICT;
        }
        if (error == OrderError.ORDER_NOT_FOUND
                || error == OrderError.PRODUCT_INACTIVE) {
            return HttpStatus.NOT_FOUND;
        }
        if (error == OrderError.COUPON_INVALID
                || error == OrderError.COUPON_EXPIRED
                || error == OrderError.COUPON_USAGE_LIMIT_REACHED) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private HttpStatus mapPaymentError(PaymentError error) {
        if (error == PaymentError.OWNERSHIP_MISMATCH) {
            return HttpStatus.FORBIDDEN;
        }
        if (error == PaymentError.INVALID_STATE_TRANSITION
                || error == PaymentError.PAYMENT_ALREADY_CAPTURED
                || error == PaymentError.PAYMENT_ALREADY_REFUNDED) {
            return HttpStatus.CONFLICT;
        }
        if (error == PaymentError.PAYMENT_NOT_FOUND
                || error == PaymentError.ORDER_NOT_PENDING) {
            return HttpStatus.NOT_FOUND;
        }
        if (error == PaymentError.CANNOT_REFUND_SHIPPED) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private HttpStatus mapStockError(StockError error) {
        if (error == StockError.INSUFFICIENT_STOCK
                || error == StockError.CAPTURE_FAILED
                || error == StockError.RESERVATION_FAILED) {
            return HttpStatus.CONFLICT;
        }
        if (error == StockError.PRODUCT_NOT_FOUND
                || error == StockError.PRODUCT_INACTIVE) {
            return HttpStatus.NOT_FOUND;
        }
        if (error == StockError.RESERVATION_FAILED
                || error == StockError.RELEASE_FAILED) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private ApiErrorResponse buildErrorResponse(HttpStatus status, String message, String path) {
        return new ApiErrorResponse(
                LocalDateTime.now(ZoneId.of("UTC")),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                null);
    }
}