package com.deodev.userService.service;

import com.deodev.userService.exception.ResourceNotFoundException;
import com.deodev.userService.model.Role;
import com.deodev.userService.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public Role getDefaultRole() {
        return roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));
    }
}
