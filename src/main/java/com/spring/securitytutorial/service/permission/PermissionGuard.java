package com.spring.securitytutorial.service.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionGuard {

    private final PermissionProvider permissionProvider;

    public boolean hasPermission(String permissionCode) {
        return permissionProvider.hasPermission(permissionCode);
    }
}
