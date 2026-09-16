package com.spring.securitytutorial.util.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RestApiHeaderNames {
    public static final String TENANT_KEY = "X-Tenant-Key";
    public static final String TENANT_SECRET = "X-Tenant-Secret";
    public static final String CURRENT_USER_ID = "X-Current-User-Id";
    public static final String WEBHOOK_KEY = "X-Webhook-Key";
    public static final String WEBHOOK_SECRET = "X-Webhook-Secret";
    public static final String WEBHOOK_TIMESTAMP = "X-Webhook-Timestamp";
    public static final String WEBHOOK_SIGNATURE = "X-Webhook-Signature";
}
