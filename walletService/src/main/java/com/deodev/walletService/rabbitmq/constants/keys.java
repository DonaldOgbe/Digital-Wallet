package com.deodev.walletService.rabbitmq.constants;

public class keys {

    // WALLET EVENTS
    public static final String WALLET_EXCHANGE = "wallet.events";
    public static final String WALLET_CREATED = "wallet.lifecycle.wallet-created";
    public static final String TRANSFER_COMPLETED = "wallet.lifecycle.transfer-completed";
    public static final String TRANSFER_FAILED = "wallet.lifecycle.transfer-failed";

    // TRANSACTION EVENTS
    public static final String WALLET_TRANSACTION_QUEUE = "wallet.transaction-events";
    public static final String TRANSACTION_EXCHANGE = "transaction.events";
    // TRANSACTION ROUTING KEYS
    public static final String P2P_TRANSACTION_WILDCARD = "transaction.p2p.lifecycle.*";
    public static final String P2P_TRANSFER_REQUESTED = "transaction.p2p.lifecycle.transfer-requested";


    // USER EVENTS
    public static final String WALLET_USER_QUEUE = "wallet.user-events";
    public static final String USER_EXCHANGE = "user.events";
    // USER ROUTING KEYS
    public static final String USER_WILDCARD_KEY = "user.lifecycle.*";
    public static final String USER_CREATED = "user.lifecycle.user-created";

    private keys() {
    }
}
