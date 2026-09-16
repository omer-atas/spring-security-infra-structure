package com.spring.securitytutorial.security.rule;

import com.spring.securitytutorial.service.model.enums.PermissionCode;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserPermissionRuleProvider implements PermissionRuleProvider {

    @Override
    public List<ApiPermissionRule> rules() {
        return List.of(
                ApiPermissionRule.rule(
                        HttpMethod.POST,
                        "/api/users/list",
                        PermissionCode.USER_LIST
                ),
                ApiPermissionRule.rule(
                        HttpMethod.POST,
                        "/api/users/create",
                        PermissionCode.USER_CREATE
                ),
                ApiPermissionRule.rule(
                        HttpMethod.DELETE,
                        "/api/users/{userId}",
                        PermissionCode.USER_DELETE
                )
        );
    }
}
