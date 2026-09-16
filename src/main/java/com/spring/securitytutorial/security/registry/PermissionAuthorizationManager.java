package com.spring.securitytutorial.security.registry;

import com.spring.securitytutorial.service.permission.PermissionProvider;
import lombok.NonNull;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class PermissionAuthorizationManager
        implements AuthorizationManager<RequestAuthorizationContext> {

    private final PermissionRegistry permissionRegistry;
    private final PermissionProvider permissionProvider;

    public PermissionAuthorizationManager(PermissionRegistry permissionRegistry,
                                          PermissionProvider permissionProvider) {
        this.permissionRegistry = permissionRegistry;
        this.permissionProvider = permissionProvider;
    }

    @Override
    public AuthorizationResult authorize(
            @NonNull Supplier<? extends Authentication> authentication,
            RequestAuthorizationContext context) {

        if (context == null) {
            return new AuthorizationDecision(false);
        }

        return permissionRegistry
                .findPermission(context.getRequest())
                .map(permissionCode ->
                        new AuthorizationDecision(
                                permissionProvider.hasPermission(permissionCode.name())
                        )
                )
                .orElseGet(() -> new AuthorizationDecision(false));
    }
}
