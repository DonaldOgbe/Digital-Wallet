package com.deodev.userService.rabbitmq.constants;

public class keys {

    public static final String USER_EXCHANGE = "user.exchange";
    public static final String USER_CREATED = "user.created";

    // DLX
    public static final String DLX = "dlx.exchange";

    // WALLET EVENTS
    public static final String WALLET_EXCHANGE = "wallet.exchange";
    public static final String WALLET_QUEUE = "wallet.queue";
    public static final String WALLET_DLQ = "wallet.dlq";

    // WALLET ROUTING KEYS
    public static final String WALLET_WILDCARD = "wallet.*";
    public static final String WALLET_CREATED = "wallet.created";

}
