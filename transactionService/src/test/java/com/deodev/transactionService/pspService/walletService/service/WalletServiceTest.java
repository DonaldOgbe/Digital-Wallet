package com.deodev.transactionService.pspService.walletService.service;

import com.deodev.transactionService.pspService.walletService.client.WalletServiceClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletServiceClient walletServiceClient;

    @InjectMocks
    private WalletService walletService;



}