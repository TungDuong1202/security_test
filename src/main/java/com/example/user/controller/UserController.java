package com.example.user.controller;

import com.example.user.dto.request.CreateUserRequest;
import com.example.user.dto.request.UpdateUserRequest;
import com.example.user.dto.response.ApiResponseEntity;
import com.example.user.dto.response.ApiResponseFactory;
import com.example.user.dto.response.UserResponse;
import com.example.user.exception.BadRequestException;
import com.example.user.service.IUserService;
import com.example.user.mapper.PageInfoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "User Management", description = "APIs for managing users (Create, Read, Update, Delete)")
public class UserController {

    private final IUserService IUserService;
    private static final String INVALID_REQUEST = "Invalid request data";
    public static final String USER_ID_REQUIRED = "User ID is required";
    public static final String DEFAULT_PAGE = "0";
    public static final String DEFAULT_SIZE = "10";
    public static final String DEFAULT_SORT_PAGE = "createdAt";

    @Operation(
            summary = "Get active users with pagination",
            description = "Retrieve a paginated list of users with status ACTIVE. Returns empty list if no users found."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "400", description = "Invalid page or size parameters", content = @Content)
    })
    @GetMapping
    public ApiResponseEntity<List<UserResponse>> getAllUsers(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = DEFAULT_PAGE)
            @Min(value = 0, message = "Page must be greater than or equal to 0")
            int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = DEFAULT_SIZE)
            @Min(value = 1, message = "Size must be greater than or equal to 1")
            int size
    ) {
        Page<UserResponse> userPage = IUserService.getAllUsers(
                PageRequest.of(page, size, Sort.by(DEFAULT_SORT_PAGE).descending())
        );
        return ApiResponseFactory.success(userPage.getContent(), PageInfoMapper.from(userPage));
    }

    @Operation(
            summary = "Create a new user",
            description = "Register a new user with profile information. Email must be unique."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error (Invalid email, password too short...) or Email already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseEntity.class)))
    })
    @PostMapping
    public ApiResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        if (Objects.isNull(request)) {
            throw new BadRequestException(INVALID_REQUEST);
        }
        return ApiResponseFactory.created(IUserService.createUser(request));
    }

    @Operation(
            summary = "Update user profile",
            description = "Update user information. Only non-null fields will be updated."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error (Invalid phone format, future birthday...)"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{id}")
    public ApiResponseEntity<UserResponse> updateUser(
            @Parameter(description = "ID of the user to be updated", required = true, example = "1")
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        if (Objects.isNull(userId)) {
            throw new BadRequestException(USER_ID_REQUIRED);
        }
        if (Objects.isNull(request)) {
            throw new BadRequestException(INVALID_REQUEST);
        }
        return ApiResponseFactory.updated(IUserService.updateUser(userId, request));
    }

    @Operation(
            summary = "Delete user (Soft Delete)",
            description = "Change user status to DELETED. User will not appear in the get list anymore."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    public ApiResponseEntity<Void> deleteUser(
            @PathVariable Long userId
    ) {
        if (Objects.isNull(userId)) {
            throw new BadRequestException(USER_ID_REQUIRED);
        }
        IUserService.deleteUser(userId);
        return ApiResponseFactory.deleted();
    }
}