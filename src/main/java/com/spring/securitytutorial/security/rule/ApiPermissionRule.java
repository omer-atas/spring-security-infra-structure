package com.spring.securitytutorial.security.rule;

import com.spring.securitytutorial.service.model.enums.PermissionCode;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class ApiPermissionRule {

    private final RequestMatcher requestMatcher;
    private final PermissionCode permissionCode;

    private ApiPermissionRule(RequestMatcher requestMatcher, PermissionCode permissionCode) {
        this.requestMatcher = requestMatcher;
        this.permissionCode = permissionCode;
    }

    public RequestMatcher getRequestMatcher() {
        return requestMatcher;
    }

    public PermissionCode getPermissionCode() {
        return permissionCode;
    }

    public static ApiPermissionRule rule(
            HttpMethod httpMethod,
            String pattern,
            PermissionCode permissionCode) {
        return new ApiPermissionRule(
                org.springframework.security.web.servlet.util.matcher
                        .PathPatternRequestMatcher.pathPattern(httpMethod, pattern),
                permissionCode
        );
    }
}
