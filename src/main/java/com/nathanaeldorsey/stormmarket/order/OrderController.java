package com.nathanaeldorsey.stormmarket.order;

import com.nathanaeldorsey.stormmarket.common.exception.ResourceNotFoundException;
import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
/**
 * HTTP entry point for order management.
 * Users can place orders from their cart and view their own order history and details.
 * Admin endpoints allow viewing and updating any order's status.
 * Guest checkout is supported via unauthenticated POST /orders with shipping details.
 */
public class OrderController {
    private final OrderService orderService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            Authentication authentication,
            @RequestBody(required = false) GuestCheckoutRequest guestRequest) {
        Long userId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            userId = getUser(authentication).getId();
        }
        OrderResponse response = orderService.placeOrder(userId, guestRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<OrderResponse> getOrderHistory(Authentication authentication) {
        User user = getUser(authentication);
        return orderService.getOrderHistory(user.getId());
    }

    @GetMapping("/{orderId:\\d+}")
    public OrderResponse getOrderDetails(Authentication authentication, @PathVariable Long orderId) {
        User user = getUser(authentication);
        return orderService.getOrderDetails(user.getId(), orderId);
    }

    @GetMapping("/admin/{orderId:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse getOrderDetailsForAdmin(@PathVariable Long orderId) {
        return orderService.getOrderDetailsForAdmin(orderId);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderResponse> getOrdersForAdmin(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);

        return orderService.getOrdersForAdmin(status, pageable);
    }

    @PatchMapping("/admin/{orderId:\\d+}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse updateOrderStatus(@PathVariable Long orderId, @RequestParam OrderStatus status, Authentication authentication) {
        User user = getUser(authentication);
        return orderService.updateOrderStatus(orderId, status, user.getUsername());
    }

    private User getUser(Authentication authentication) {
        return userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
