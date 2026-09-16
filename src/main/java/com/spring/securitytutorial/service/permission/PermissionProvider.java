package com.spring.securitytutorial.service.permission;

import java.util.Set;

public interface PermissionProvider {

    boolean hasPermission(String permission);

    Set<String> getPermissions();
}
