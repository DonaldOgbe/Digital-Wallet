package com.deodev.userService.rabbitmq.constants;

public class keys {
    public static final String USER_EXCHANGE = "user.events";
    public static final String USER_CREATED = "user.lifecycle.user-created";


    // WALLET EVENTS
    public static final String USER_WALLET_QUEUE = "user.wallet-events";
    public static final String WALLET_EXCHANGE = "wallet.events";
    public static final String WALLET_WILDCARD_KEY = "wallet.lifecycle.*";

    // WALLET ROUTING KEYS
    public static final String WALLET_CREATED = "wallet.lifecycle.wallet-created";
}
