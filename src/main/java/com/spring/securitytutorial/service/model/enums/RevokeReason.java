package com.spring.securitytutorial.service.model.enums;

public enum RevokeReason {
    NEW_LOGIN,
    ADMIN_REVOKE,
    USER_DISABLED,
    IDLE_TIMEOUT,
    REFRESH_TOKEN_EXPIRED,
    ABSOLUTE_TIMEOUT,
    REFRESH_TOKEN_REUSE
}
