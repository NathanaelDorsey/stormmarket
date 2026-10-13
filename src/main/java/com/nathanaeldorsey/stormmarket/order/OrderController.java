package com.nathanaeldorsey.stormmarket.order;

import com.nathanaeldorsey.stormmarket.user.User;
import com.nathanaeldorsey.stormmarket.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(Authentication authentication){
        User user = getUser(authentication);
        OrderResponse response =  orderService.placeOrder(user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<OrderResponse> getOrderHistory(Authentication authentication) {
        User user = getUser(authentication);
        return orderService.getOrderHistory(user.getId());
    }

    private User getUser(Authentication authentication){
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }
}
