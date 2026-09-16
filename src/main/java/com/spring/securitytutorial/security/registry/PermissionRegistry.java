package com.spring.securitytutorial.security.registry;

import com.spring.securitytutorial.service.model.enums.PermissionCode;
import com.spring.securitytutorial.security.rule.ApiPermissionRule;
import com.spring.securitytutorial.security.rule.PermissionRuleProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PermissionRegistry {

    private final List<ApiPermissionRule> rules;

    public PermissionRegistry(List<PermissionRuleProvider> providers) {
        this.rules = providers.stream()
                .flatMap(provider -> provider.rules().stream())
                .toList();
    }

    public Optional<PermissionCode> findPermission(
            HttpServletRequest httpServletRequest) {

        return rules.stream()
                .filter(rule ->
                        rule.getRequestMatcher().matches(httpServletRequest))
                .map(ApiPermissionRule::getPermissionCode)
                .findFirst();
    }
}
