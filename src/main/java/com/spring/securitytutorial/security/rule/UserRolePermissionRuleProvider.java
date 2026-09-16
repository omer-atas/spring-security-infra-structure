package com.spring.securitytutorial.security.rule;

import com.spring.securitytutorial.service.model.enums.PermissionCode;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserRolePermissionRuleProvider implements PermissionRuleProvider {

    @Override
    public List<ApiPermissionRule> rules() {
        return List.of(
                ApiPermissionRule.rule(
                        HttpMethod.POST,
                        "/api/roles/list",
                        PermissionCode.USER_ROLES_LIST
                )
        );
    }
}
