package com.spring.securitytutorial.security.rule;

import com.spring.securitytutorial.service.model.enums.PermissionCode;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SecurityAdminPermissionRuleProvider implements PermissionRuleProvider {

    @Override
    public List<ApiPermissionRule> rules() {
        return List.of(
                ApiPermissionRule.rule(
                        HttpMethod.GET,
                        "/api/admin/users",
                        PermissionCode.USER_LIST
                ),
                ApiPermissionRule.rule(
                        HttpMethod.GET,
                        "/api/admin/sessions",
                        PermissionCode.SESSION_LIST
                ),
                ApiPermissionRule.rule(
                        HttpMethod.POST,
                        "/api/admin/sessions/{sessionId}/revoke",
                        PermissionCode.SESSION_REVOKE
                )
        );
    }
}