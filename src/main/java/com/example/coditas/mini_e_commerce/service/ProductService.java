package com.example.coditas.mini_e_commerce.service;

import com.example.coditas.mini_e_commerce.dto.*;
import com.example.coditas.mini_e_commerce.entity.Product;
import com.example.coditas.mini_e_commerce.entity.User;
import com.example.coditas.mini_e_commerce.enums.ActiveStatus;
import com.example.coditas.mini_e_commerce.enums.Category;
import com.example.coditas.mini_e_commerce.enums.UserRole;
import com.example.coditas.mini_e_commerce.exception.CustomException;
import com.example.coditas.mini_e_commerce.repository.ProductRepository;
import com.example.coditas.mini_e_commerce.repository.UserRepository;
import com.example.coditas.mini_e_commerce.specification.GenericFilterSpecFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Page<ProductResponseDto> searchProducts(GenericFilterDto filter, PageableDto pageReq) {
        Specification<Product> spec = GenericFilterSpecFactory.forProduct(filter);
        Pageable pageable = toPageableForProduct(pageReq);
        Page<Product> page = productRepository.findAll(spec, pageable);
        return page.map(this::toDto);
    }

    public Page<ProductResponseDto> globalSearch(String query, PageableDto pageReq) {
        Specification<Product> spec = GenericFilterSpecFactory.globalSearch(
                new GenericFilterDto(){{setName(query);}}, "name"
        );
        Pageable pageable = toPageableForProduct(pageReq);
        Page<Product> page = productRepository.findAll(spec, pageable);
        return page.map(this::toDto);
    }

    public ProductResponseDto getProductDetail(String id) {
        Product product = productRepository.findByProductId(id)
                .orElseThrow(() -> new CustomException("Product not found", HttpStatus.NOT_FOUND));
        return toDto(product);
    }

    @Transactional
    public ProductResponseDto createProduct(ProductCreateRequestDto dto) {
        String productId = String.valueOf(UUID.randomUUID());

        User currentUser = getCurrentUser();

        Product product = Product.builder()
                .productId(productId)
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .price(dto.getUnitPrice())
                .category(Category.valueOf(dto.getCategory().toUpperCase()))
                .stockQuantity(dto.getStock())
                .vendor(currentUser)
                .isActive(ActiveStatus.ACTIVE)
                .build();

        product = productRepository.save(product);

        log.info("Product created: {} ({})", product.getName(), product.getProductId());
        return toDto(product);
    }

    @Transactional
    public ProductResponseDto updateProduct(String id, ProductUpdateRequestDto dto) {
        Product product = productRepository.findByProductId(id)
                .orElseThrow(() -> new CustomException("Product not found", HttpStatus.NOT_FOUND));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            product.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null && dto.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            product.setPrice(dto.getPrice());
        }
        if (dto.getCategory() != null) {
            Category category = Category.valueOf(dto.getCategory().toUpperCase());
            product.setCategory(category);
        }
        if (dto.getStock() != null) {
            product.setStockQuantity(dto.getStock());
        }

        product = productRepository.save(product);

        return toDto(product);
    }

    @Transactional
    public String softDeleteProduct(String id) {

        Product product = productRepository.findByProductId(id)
                .orElseThrow(() -> new CustomException("Product not found", HttpStatus.NOT_FOUND));

        User currentUser = getCurrentUser();
        if(!currentUser.equals(product.getVendor()) || currentUser.getRole()!= UserRole.ADMIN){
            throw new CustomException("Product can only be deleted by creator or admin", HttpStatus.CONFLICT);
        }

        if (product.getIsActive() == ActiveStatus.INACTIVE) {
            throw new CustomException("Product already deleted", HttpStatus.CONFLICT);
        }
        product.setIsActive(ActiveStatus.INACTIVE);
        productRepository.save(product);
        return "Product deleted successfully!";
    }

    private Pageable toPageableForProduct(PageableDto dto) {
        String field = switch (dto.getSortBy().toLowerCase()) {
            case "name" -> "name";
            case "price", "unit_price" -> "unitPrice";
            case "category" -> "category.name";
            case "created_on", "createdat" -> "createdAt";
            default -> "createdAt";
        };
        Sort sort = "desc".equalsIgnoreCase(dto.getSortDir())
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();
        return PageRequest.of(dto.getPage(), dto.getSize(), sort);
    }

    private ProductResponseDto toDto(Product p) {
        return ProductResponseDto.builder()
                .productId(p.getProductId())
                .name(p.getName())
                .description(p.getDescription())
                .imageUrl(p.getImageUrl())
                .unitPrice(p.getPrice())
                .categoryName(String.valueOf(p.getCategory()))
                .status(p.getIsActive().name())
                .createdOn(p.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                .currentStock(p.getStockQuantity())
                .build();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
    }
}
