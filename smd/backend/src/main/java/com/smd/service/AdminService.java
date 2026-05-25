package com.smd.service;

import com.smd.dto.UserRequest;
import com.smd.dto.UserResponse;
import com.smd.entity.Role;
import com.smd.entity.User;
import com.smd.exception.ApiException;
import com.smd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    public UserResponse createUser(UserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already in use: " + req.getEmail());
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password is required when creating a user");
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setRole(Role.valueOf(req.getRole()));
        user.setDepartment(req.getDepartment());
        user.setIsActive(true);

        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse updateUser(Long id, UserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (!user.getEmail().equals(req.getEmail()) && userRepository.existsByEmail(req.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already in use: " + req.getEmail());
        }

        user.setEmail(req.getEmail());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setRole(Role.valueOf(req.getRole()));
        user.setDepartment(req.getDepartment());

        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }

        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse toggleStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
        return UserResponse.from(userRepository.save(user));
    }
}
