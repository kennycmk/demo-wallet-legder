package com.altech.walletledger.constant;

public final class AppConstants {

    private AppConstants() {
    }

    public static final String API_V1 = "/api/v1";
    public static final String AUTH_BASE = API_V1 + "/auth";
    public static final String WALLET_ME_BASE = API_V1 + "/wallets/me";
    public static final String WEBHOOK_BASE = API_V1 + "/webhooks";

    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String JWT_SCHEME = "bearer-jwt";
    public static final String TRANSFER_OUT_SUFFIX = ":out";
    public static final String TRANSFER_IN_SUFFIX = ":in";

    public static final String SUCCESS_MESSAGE = "OK";
    public static final String UNAUTHORIZED_MESSAGE = "Unauthorized";

    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 72;
    public static final int IDEMPOTENCY_KEY_MAX_LENGTH = 128;

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    public static final String[] PUBLIC_AUTH = {AUTH_BASE + "/**"};
    public static final String[] PUBLIC_WEBHOOKS = {WEBHOOK_BASE + "/**"};
    public static final String[] PUBLIC_SWAGGER = {"/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"};
    public static final String[] PUBLIC_ACTUATOR = {"/actuator/health", "/actuator/health/**"};
}
