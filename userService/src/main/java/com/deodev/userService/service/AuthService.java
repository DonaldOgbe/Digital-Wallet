package com.deodev.userService.service;

import com.deodev.userService.dto.request.UserLoginRequest;
import com.deodev.userService.dto.request.UserRegistrationRequest;
import com.deodev.userService.dto.response.ApiResponse;
import com.deodev.userService.exception.UserAlreadyExistsException;
import com.deodev.userService.model.Role;
import com.deodev.userService.model.User;
import com.deodev.userService.model.enums.UserStatus;
import com.deodev.userService.repository.RoleRepository;
import com.deodev.userService.repository.UserRepository;
import com.deodev.userService.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public ApiResponse<Void> register(UserRegistrationRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email Already Exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username Already Exists");
        }

        User user = new ObjectMapper().convertValue(request, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRoles(Set.of(defaultRole));

        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        return ApiResponse.success("User Registered Successfully", null);
    }

    public ApiResponse<String> login(UserLoginRequest request) {
        try {
            Authentication loginAuthentication =  authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            UserDetails userDetails = (UserDetails) loginAuthentication.getPrincipal();

            List<String> authorities = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            Map<String, Object> claims = new HashMap<>();
            claims.put("authorities", authorities);

            String jwt = jwtUtil.generateToken(claims, userDetails.getUsername());
            SecurityContextHolder.getContext().setAuthentication(jwtUtil.getAuthenticationFromToken(jwt));

            return ApiResponse.success("Login Successful", jwt);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
