package com.spring.securitytutorial.security.rule;

import java.util.List;

public interface PermissionRuleProvider {

    List<ApiPermissionRule> rules();
}
