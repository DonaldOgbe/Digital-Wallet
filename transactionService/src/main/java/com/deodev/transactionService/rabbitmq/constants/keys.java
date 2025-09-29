package com.deodev.transactionService.rabbitmq.constants;

public class keys {

    // TRANSACTION EVENTS
    public static final String TRANSACTION_EXCHANGE = "transaction.events";
    public static final String P2P_TRANSFER_REQUESTED = "transaction.p2p.lifecycle.transfer-requested";

    // WALLET EVENTS
    public static final String TRANSACTION_WALLET_QUEUE = "transaction.wallet-events";
    public static final String WALLET_EXCHANGE = "wallet.events";
    public static final String WALLET_WILDCARD_KEY = "wallet.lifecycle.*";

    // USER EVENTS
    public static final String WALLET_USER_QUEUE = "wallet.user-events";
    public static final String USER_EXCHANGE = "user.events";
    public static final String USER_WILDCARD_KEY = "user.lifecycle.*";

    // USER ROUTING KEYS
    public static final String USER_CREATED = "user.lifecycle.user-created";

    private keys() {
    }
}
