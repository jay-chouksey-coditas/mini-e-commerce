package com.example.coditas.mini_e_commerce.specification;

import com.example.coditas.mini_e_commerce.dto.GenericFilterDto;
import com.example.coditas.mini_e_commerce.entity.Order;
import com.example.coditas.mini_e_commerce.entity.Product;
import com.example.coditas.mini_e_commerce.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class GenericFilterSpecFactory {

    private GenericFilterSpecFactory() {}

    private static final String IS_ACTIVE = "isActive";

    // PRODUCT
    public static Specification<Product> forProduct(GenericFilterDto filter) {
        return GenericSpecificationBuilder.<Product>builder()
                .add(enumEqual("category", EnumHelper.toCategory(filter.getCategory())))
                .add(greaterThanEqual("price", filter.getMinPrice()))
                .add(lessThanEqual("price", filter.getMaxPrice()))
                .add(enumEqual(IS_ACTIVE, EnumHelper.toActiveStatus(filter.getStatus())))
                .build();
    }

    // USER
    public static Specification<User> forUser(GenericFilterDto filter) {
        return GenericSpecificationBuilder.<User>builder()
                .add(enumEqual("role", EnumHelper.toRole(filter.getRole())))
                .add(enumEqual(IS_ACTIVE, EnumHelper.toActiveStatus(filter.getStatus())))
                .build();
    }

    // ORDER
    public static Specification<Order> forOrder(User currentUser, GenericFilterDto filter) {
        return GenericSpecificationBuilder.<Order>builder()

                .add(enumEqual("status", EnumHelper.toOrderStatus(filter.getStatus()))) // e.g. ?status=PENDING
                .add(greaterThanEqual("totalPrice", filter.getMinPrice()))
                .add(lessThanEqual("totalPrice", filter.getMaxPrice()))
                .add(between("createdAt", filter.getStartDate(), filter.getEndDate())) // ?startDate=2025-01-01&endDate=2025-01-31

                // Global search on orderId
                .add(globalSearch(filter.getName(), "orderId"))

                .build();
    }


    // GLOBAL SEARCH
    public static <T> Specification<T> globalSearch(GenericFilterDto filter, String... fields) {
        if (!hasText(filter.getName())) return (root, query, cb) -> cb.conjunction();
        return GenericSpecificationBuilder.<T>builder()
                .add(globalSearch(filter.getName(), fields))
                .build();
    }

    // === HELPER METHODS ===
    private static <T> GenericSpecificationBuilder.SpecPart<T> like(String field, String value) {
        return (root, cb) -> hasText(value)
                ? cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%")
                : null;
    }

    private static <T> GenericSpecificationBuilder.SpecPart<T> equal(String field, Object value) {
        return (root, cb) -> value != null ? cb.equal(root.get(field), value) : null;
    }

    private static <T> GenericSpecificationBuilder.SpecPart<T> greaterThanEqual(String field, BigDecimal value) {
        return (root, cb) -> value != null ? cb.greaterThanOrEqualTo(root.get(field), value) : null;
    }

    private static <T> GenericSpecificationBuilder.SpecPart<T> lessThanEqual(String field, BigDecimal value) {
        return (root, cb) -> value != null ? cb.lessThanOrEqualTo(root.get(field), value) : null;
    }

    private static <T> GenericSpecificationBuilder.SpecPart<T> between(String field, LocalDate start, LocalDate end) {
        return (root, cb) -> {
            if (start == null || end == null) return null;
            ZonedDateTime s = start.atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime e = end.atTime(23, 59, 59, 999999999).atZone(ZoneId.systemDefault());
            return cb.between(root.get(field), s, e);
        };
    }

    private static <T> GenericSpecificationBuilder.SpecPart<T> joinLike(String join, String field, String value) {
        return (root, cb) -> {
            if (!hasText(value)) return null;
            Join<T, ?> j = root.join(join, JoinType.LEFT);
            return cb.like(cb.lower(j.get(field)), "%" + value.toLowerCase() + "%");
        };
    }

    private static <T> GenericSpecificationBuilder.SpecPart<T> joinEqual(String join, String field, Object value) {
        return (root, cb) -> {
            if (value == null) return null;
            Join<T, ?> j = root.join(join, JoinType.LEFT);
            return cb.equal(j.get(field), value);
        };
    }

    private static <T> GenericSpecificationBuilder.SpecPart<T> joinEqualIf(String join, String field, Object value) {
        return (root, cb) -> {
            if (value == null || !hasText(value.toString())) return null;
            Join<T, ?> j = root.join(join, JoinType.INNER);
            return cb.equal(j.get(field), value);
        };
    }

    private static <T> GenericSpecificationBuilder.SpecPart<T> globalSearch(String query, String... fields) {
        return (root, cb) -> {
            if (!hasText(query)) return null;
            String like = "%" + query.toLowerCase() + "%";
            List<Predicate> ors = new ArrayList<>();
            for (String f : fields) {
                if (f.contains(".")) {
                    String[] parts = f.split("\\.", 2);
                    Join<T, ?> j = root.join(parts[0], JoinType.LEFT);
                    ors.add(cb.like(cb.lower(j.get(parts[1])), like));
                } else {
                    ors.add(cb.like(cb.lower(root.get(f)), like));
                }
            }
            return cb.or(ors.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private static <T> GenericSpecificationBuilder.SpecPart<T> enumEqual(String field, Enum<?> value) {
        return (root, cb) -> value != null ? cb.equal(root.get(field), value) : null;
    }
}