package com.spring.securitytutorial.service.permission;

import com.spring.securitytutorial.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final UserService users;

    public Set<String> findActivePermissions(UUID userId) {
        return users.findById(userId)
                .map(user -> Set.copyOf(user.permissions()))
                .orElseGet(Set::of);
    }
}
