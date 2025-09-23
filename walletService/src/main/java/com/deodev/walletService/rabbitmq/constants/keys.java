package com.deodev.walletService.rabbitmq.constants;

public class keys {

    // WALLET EVENTS
    public static final String WALLET_EXCHANGE = "wallet.events";
    public static final String WALLET_CREATED = "wallet.lifecycle.wallet-created";

    // TRANSACTION EVENTS
    public static final String WALLET_TRANSACTION_QUEUE = "wallet.transaction-events";
    public static final String TRANSACTION_EXCHANGE = "transaction.events";
    public static final String TRANSACTION_P2P_WILDCARD = "transaction.p2p.lifecycle.*";

    // USER EVENTS
    public static final String WALLET_USER_QUEUE = "wallet.user-events";
    public static final String USER_EXCHANGE = "user.events";
    public static final String USER_REGISTRATION_WILDCARD = "user.lifecycle.*";

    // USER ROUTING KEYS
    public static final String USER_CREATED = "user.lifecycle.user-created";

    private keys() {
    }
}
