package com.example.coditas.mini_e_commerce.service;

import com.example.coditas.mini_e_commerce.dto.AddToCartRequestDto;
import com.example.coditas.mini_e_commerce.dto.CartItemResponseDto;
import com.example.coditas.mini_e_commerce.dto.CartResponseDto;
import com.example.coditas.mini_e_commerce.entity.Cart;
import com.example.coditas.mini_e_commerce.entity.CartItem;
import com.example.coditas.mini_e_commerce.entity.Product;
import com.example.coditas.mini_e_commerce.entity.User;
import com.example.coditas.mini_e_commerce.exception.CustomException;
import com.example.coditas.mini_e_commerce.repository.CartRepository;
import com.example.coditas.mini_e_commerce.repository.ProductRepository;
import com.example.coditas.mini_e_commerce.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartResponseDto getCurrentUserCart() {
        Cart cart = getOrCreateCart();
        return toDto(cart);
    }

    public CartResponseDto addToCart(AddToCartRequestDto dto) {
        Cart cart = getOrCreateCart();
        Product product = productRepository.findByProductId(dto.getProductId())
                .orElseThrow(() -> new CustomException("Product not found", HttpStatus.NOT_FOUND));

        if (product.getStockQuantity() < dto.getQuantity()) {
            throw new CustomException("Only " + product.getStockQuantity() + " items in stock", HttpStatus.BAD_REQUEST);
        }

        cart.getItems().stream()
                .filter(item -> item.getProduct().equals(product))
                .findFirst()
                .ifPresentOrElse(
                        item -> {
                            int newQty = item.getQuantity() + dto.getQuantity();
                            if (product.getStockQuantity() < newQty) {
                                throw new CustomException("Not enough stock", HttpStatus.BAD_REQUEST);
                            }
                            item.setQuantity(newQty);
                            item.setUnitPrice(product.getPrice());
                        },
                        () -> {
                            CartItem newItem = new CartItem();
                            newItem.setCart(cart);
                            newItem.setProduct(product);
                            newItem.setQuantity(dto.getQuantity());
                            newItem.setUnitPrice(product.getPrice());
                            cart.getItems().add(newItem);
                        }
                );

        cart.recalculateTotals();
        cartRepository.save(cart);
        log.info("Added {} x {} to cart of user {}", dto.getQuantity(), product.getName(), getCurrentUser().getEmail());
        return toDto(cart);
    }

    public CartResponseDto updateQuantity(String productId, Integer newQuantity) {
        if (newQuantity < 1) {
            return removeFromCart(productId);
        }

        Cart cart = getOrCreateCart();
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new CustomException("Product not in cart", HttpStatus.NOT_FOUND));

        if (product.getStockQuantity() < newQuantity) {
            throw new CustomException("Only " + product.getStockQuantity() + " left in stock", HttpStatus.BAD_REQUEST);
        }

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CustomException("Product not in cart", HttpStatus.NOT_FOUND));


        item.setQuantity(newQuantity);
        item.setUnitPrice(product.getPrice());

        cart.recalculateTotals();

        cartRepository.save(cart);
        return toDto(cart);
    }

    public CartResponseDto removeFromCart(String productId) {
        Cart cart = getOrCreateCart();
        boolean removed = cart.getItems().removeIf(item -> item.getProduct().getProductId().equals(productId));
        if (!removed) {
            throw new CustomException("Product not in cart", HttpStatus.NOT_FOUND);
        }

        cart.recalculateTotals();
        cartRepository.save(cart);
        return toDto(cart);
    }

    @Transactional
    public void clearCart() {
        User user = getCurrentUser();
        cartRepository.findByUser(user).ifPresent(cart -> {
            cart.clear();
            cartRepository.save(cart);
        });
    }

    // Used by OrderService after successful order
    @Transactional
    public void clearCart(User user) {
        cartRepository.findByUser(user).ifPresent(cart -> {
            cart.clear();
            cartRepository.save(cart);
        });
    }

    private Cart getOrCreateCart() {
        User user = getCurrentUser();
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    private CartResponseDto toDto(Cart cart) {
        if (cart == null || cart.getItems().isEmpty()) {
            return new CartResponseDto(0, BigDecimal.ZERO, List.of());
        }

        List<CartItemResponseDto> items = cart.getItems().stream()
                .map(item -> new CartItemResponseDto(
                        item.getProduct().getProductId(),
                        item.getProduct().getName(),
                        item.getProduct().getImageUrl(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();

        return new CartResponseDto(
                cart.getTotalQuantity(),
                cart.getTotalPrice(),
                items
        );
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.UNAUTHORIZED));
    }
}
