package com.spring.securitytutorial.service.permission;

import com.spring.securitytutorial.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Set;

@Component
@RequestScope
@RequiredArgsConstructor
public class RequestPermissionProvider implements PermissionProvider {

    private final UserService users;
    private final PermissionService permissionService;

    private boolean resolved;
    private Set<String> permissions = Set.of();

    @Override
    public boolean hasPermission(String permission) {
        return getPermissions().contains(permission);
    }

    @Override
    public Set<String> getPermissions() {
        if (!resolved) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Set<String> fromUser = users.findByUsername(authentication.getName())
                        .map(user -> permissionService.findActivePermissions(user.id()))
                        .orElseGet(Set::of);
                Set<String> fromAuthorities = authentication.getAuthorities().stream()
                        .map(String::valueOf)
                        .collect(java.util.stream.Collectors.toSet());
                java.util.HashSet<String> merged = new java.util.HashSet<>(fromUser);
                merged.addAll(fromAuthorities);
                permissions = Set.copyOf(merged);
            }
            resolved = true;
        }
        return permissions;
    }
}
