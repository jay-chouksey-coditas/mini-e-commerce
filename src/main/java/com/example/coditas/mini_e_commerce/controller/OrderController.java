package com.example.coditas.mini_e_commerce.controller;

import com.example.coditas.mini_e_commerce.dto.*;
import com.example.coditas.mini_e_commerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<OrderResponseDto>> placeOrder(
            @Valid @RequestBody OrderCreateRequestDto dto) {
        OrderResponseDto data = orderService.placeOrder(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.ok(data, "Order placed successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<OrderResponseDto>>> getOrders(
            @ModelAttribute GenericFilterDto filter,
            @ModelAttribute PageableDto page) {
        Page<OrderResponseDto> data = orderService.getOrdersForCurrentUser(filter, page);
        return ResponseEntity.ok(ApiResponseDto.paged(data, page.getPage(), page.getSize(), data.getTotalElements()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponseDto<OrderResponseDto>> getOrderDetail(@PathVariable String orderId) {
        OrderResponseDto data = orderService.getOrderDetailForCurrentUser(orderId);
        return ResponseEntity.ok(ApiResponseDto.ok(data, "Order details"));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponseDto<OrderResponseDto>> updateStatus(
            @PathVariable String orderId,
            @Valid @RequestBody OrderUpdateStatusRequestDto dto) {
        OrderResponseDto data = orderService.updateOrderStatus(orderId, dto.getStatus());
        return ResponseEntity.ok(ApiResponseDto.ok(data, "Order status updated"));
    }
}
