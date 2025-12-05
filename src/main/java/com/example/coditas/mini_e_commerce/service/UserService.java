package com.example.coditas.mini_e_commerce.service;

import com.example.coditas.mini_e_commerce.dto.GenericFilterDto;
import com.example.coditas.mini_e_commerce.dto.PageableDto;
import com.example.coditas.mini_e_commerce.dto.UserResponseDto;
import com.example.coditas.mini_e_commerce.dto.UserUpdateRequestDto;
import com.example.coditas.mini_e_commerce.entity.User;
import com.example.coditas.mini_e_commerce.enums.ActiveStatus;
import com.example.coditas.mini_e_commerce.enums.UserRole;
import com.example.coditas.mini_e_commerce.exception.CustomException;
import com.example.coditas.mini_e_commerce.repository.UserRepository;
import com.example.coditas.mini_e_commerce.specification.GenericFilterSpecFactory;
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

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponseDto updateUser(String userId, UserUpdateRequestDto dto) {

        // Find user (must be ACTIVE)
        User user = userRepository.findByUserIdAndIsActive(userId, ActiveStatus.ACTIVE)
                .orElseThrow(() -> new CustomException("User not found or inactive", HttpStatus.NOT_FOUND));


        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        boolean isSelf = currentUser.getUserId().equals(userId);

        if (!isSelf) {
            throw new CustomException("You can only update your own profile", HttpStatus.FORBIDDEN);
        }

        if (dto.getName() != null && !dto.getName().trim().isBlank()) {
            user.setName(dto.getName().trim());
        }

        boolean isAdmin = currentUser.getRole().equals(UserRole.ADMIN);

        if(dto.getRole() != null && !dto.getRole().trim().isBlank() && isAdmin){
            user.setRole(UserRole.valueOf(dto.getRole().toUpperCase()));
        }

        user = userRepository.save(user);

        log.info("User updated: {} by {}", userId, currentUser.getName());

        return toDto(user);
    }

    @Transactional
    public String softDeleteUser(String userId) {

        // Find user
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException("User not found: " + userId, HttpStatus.NOT_FOUND));


        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (!currentUser.equals(user) || currentUser.getRole().equals(UserRole.ADMIN)) {
            throw new CustomException("User can only be deleted by the user himself and admin!", HttpStatus.FORBIDDEN);
        }


        // Prevent deleting admins
        if (user.getRole().equals(UserRole.ADMIN)) {
            throw new CustomException("Cannot delete ADMIN users", HttpStatus.FORBIDDEN);
        }

        // Already inactive check
        if (user.getIsActive() == ActiveStatus.INACTIVE) {
            log.info("User already deleted (soft): {}", userId);
            throw new CustomException("User already deleted", HttpStatus.CONFLICT);
        }

        // Soft delete
        user.setIsActive(ActiveStatus.INACTIVE);

        userRepository.save(user);

        log.info("User soft deleted: {} by {}", userId, currentUser != null ? currentUser.getUserId() : "system");

        return "User delete successfully!";
    }

    public Page<UserResponseDto> searchUsers(GenericFilterDto filter, PageableDto pageReq) {
        Specification<User> spec = GenericFilterSpecFactory.forUser(filter);

        Pageable pageable = toPageable(pageReq);

        Page<User> page = userRepository.findAll(spec, pageable);

        return page.map(UserService::toDto);
    }

    public Page<UserResponseDto> globalSearch(String query, PageableDto pageReq) {
        Specification<User> spec = GenericFilterSpecFactory.globalSearch(
                new GenericFilterDto(){{setName(query);}}, "name"
        );

        Pageable pageable = toPageable(pageReq);

        Page<User> page = userRepository.findAll(spec, pageable);

        return page.map(UserService::toDto);
    }

    private static UserResponseDto toDto(User u) {
        return UserResponseDto.builder()
                .userId(u.getUserId())
                .name(u.getName())
                .email(u.getEmail())
                .role(String.valueOf(u.getRole()))
                .isActive(String.valueOf(u.getIsActive()))
                .build();
    }


    private Pageable toPageable(PageableDto dto) {
        String sortField = "user_id".equalsIgnoreCase(dto.getSortBy())
                ? "userId"
                : dto.getSortBy();

        Sort sort = "desc".equalsIgnoreCase(dto.getSortDir())
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        return PageRequest.of(dto.getPage(), dto.getSize(), sort);
    }
}
