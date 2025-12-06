package com.example.coditas.mini_e_commerce.service;

import com.example.coditas.mini_e_commerce.dto.*;
import com.example.coditas.mini_e_commerce.entity.Order;
import com.example.coditas.mini_e_commerce.entity.OrderItem;
import com.example.coditas.mini_e_commerce.entity.Product;
import com.example.coditas.mini_e_commerce.entity.User;
import com.example.coditas.mini_e_commerce.enums.OrderStatus;
import com.example.coditas.mini_e_commerce.enums.UserRole;
import com.example.coditas.mini_e_commerce.exception.CustomException;
import com.example.coditas.mini_e_commerce.repository.OrderRepository;
import com.example.coditas.mini_e_commerce.repository.ProductRepository;
import com.example.coditas.mini_e_commerce.repository.UserRepository;
import com.example.coditas.mini_e_commerce.specification.GenericFilterSpecFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final UserRepository userRepository;

    public OrderResponseDto placeOrder(OrderCreateRequestDto dto) {
        User customer = getCurrentUser();
        if (!customer.getRole().equals(UserRole.CUSTOMER)) {
            throw new CustomException("Only customers can place orders", HttpStatus.FORBIDDEN);
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalQty = 0;

        for (OrderItemRequestDto itemDto : dto.getItems()) {
            Product product = productRepository.findByProductId(itemDto.getProductId())
                    .orElseThrow(() -> new CustomException("Product not found: " + itemDto.getProductId(), HttpStatus.NOT_FOUND));

            if (product.getStockQuantity() < itemDto.getQuantity()) {
                throw new CustomException("Insufficient stock for product: " + product.getName(), HttpStatus.BAD_REQUEST);
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setUnitPrice(product.getPrice());

            order.getItems().add(orderItem);

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - itemDto.getQuantity());
            productRepository.save(product);

            totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            totalQty += itemDto.getQuantity();
        }

        order.setTotalPrice(totalPrice);
        order.setTotalQuantity(totalQty);

        order = orderRepository.save(order);

        cartService.clearCart(customer);

        log.info("Order placed: {} by {}", order.getOrderId(), customer.getEmail());
        return toDto(order);
    }

    public Page<OrderResponseDto> getOrdersForCurrentUser(GenericFilterDto filter, PageableDto pageReq) {
        User currentUser = getCurrentUser();

        Pageable pageable = PageRequest.of(
                pageReq.getPage(),
                pageReq.getSize(),
                Sort.by("createdAt").descending()
        );

        Specification<Order> spec = GenericFilterSpecFactory.forOrder(currentUser, filter);

        Page<Order> page = orderRepository.findAll(spec, pageable);

        return page.map(this::toDto);
    }

    public OrderResponseDto getOrderDetailForCurrentUser(String orderId) {
        User currentUser = getCurrentUser();
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException("Order not found", HttpStatus.NOT_FOUND));

        boolean canAccess = currentUser.getRole() == UserRole.ADMIN
                || order.getCustomer().equals(currentUser)
                || order.getItems().stream()
                .anyMatch(item -> item.getProduct().getVendor().equals(currentUser));

        if (!canAccess) {
            throw new CustomException("Access denied to this order", HttpStatus.FORBIDDEN);
        }

        return toDto(order);
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(String orderId, String statusStr) {
        User currentUser = getCurrentUser();
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException("Order not found", HttpStatus.NOT_FOUND));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException("Invalid status: " + statusStr, HttpStatus.BAD_REQUEST);
        }

        boolean isVendorOfAnyItem = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getVendor().equals(currentUser));

        if (currentUser.getRole() == UserRole.VENDOR && !isVendorOfAnyItem) {
            throw new CustomException("You can only update orders containing your products", HttpStatus.FORBIDDEN);
        }

        if (currentUser.getRole() == UserRole.CUSTOMER) {
            throw new CustomException("Customers cannot update order status", HttpStatus.FORBIDDEN);
        }

        order.setStatus(newStatus);
        order = orderRepository.save(order);

        log.info("Order {} status updated to {} by {}", orderId, newStatus, currentUser.getEmail());
        return toDto(order);
    }

    private OrderResponseDto toDto(Order order) {
        List<OrderItemResponseDto> items = order.getItems().stream()
                .map(item -> new OrderItemResponseDto(
                        item.getProduct().getProductId(),
                        item.getProduct().getName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();

        return new OrderResponseDto(
                order.getOrderId(),
                order.getStatus().name(),
                order.getTotalPrice(),
                order.getTotalQuantity(),
                order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")),
                items
        );
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.UNAUTHORIZED));
    }
}
