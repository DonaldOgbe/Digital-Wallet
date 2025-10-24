package com.deodev.walletService.rabbitmq.constants;

public class keys {

    // WALLET EVENTS
    public static final String WALLET_EXCHANGE = "wallet.exchange";
    public static final String WALLET_CREATED = "wallet.created";

    // DLX
    public static final String DLX = "dlx.exchange";

    // TRANSACTION EVENTS
    public static final String TRANSACTION_EXCHANGE = "transaction.exchange";
    public static final String TRANSACTION_QUEUE = "transaction.queue";
    public static final String TRANSACTION_DLQ = "transaction.dlq";
    // TRANSACTION ROUTING KEYS
    public static final String TRANSACTION_WILDCARD = "transaction.#";
    public static final String ACCOUNT_FUNDED = "transaction.account.funded";

    // USER EVENTS
    public static final String USER_EXCHANGE = "user.exchange";
    public static final String USER_QUEUE = "user.queue";
    public static final String USER_DLQ = "user.dlq";
    // USER ROUTING KEYS
    public static final String USER_WILDCARD = "user.#";
    public static final String USER_CREATED = "user.created";

    private keys() {
    }
}
