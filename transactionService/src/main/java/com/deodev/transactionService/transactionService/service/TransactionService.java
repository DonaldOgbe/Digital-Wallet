package com.deodev.transactionService.transactionService.service;

import com.deodev.transactionService.exception.ResourceNotFoundException;
import com.deodev.transactionService.pspService.walletService.client.WalletServiceClient;
import com.deodev.transactionService.enums.ErrorCode;
import com.deodev.transactionService.enums.TransactionStatus;
import com.deodev.transactionService.transactionService.model.CardFundingTransaction;
import com.deodev.transactionService.transactionService.model.P2PTransaction;
import com.deodev.transactionService.transactionService.model.Transaction;
import com.deodev.transactionService.transactionService.repository.CardFundingTransactionRepository;
import com.deodev.transactionService.transactionService.repository.P2PTransactionRepository;
import com.deodev.transactionService.transactionService.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final P2PTransactionRepository p2pTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final CardFundingTransactionRepository cardFundingTransactionRepository;
    private final WalletServiceClient walletServiceClient;
    private final ObjectMapper mapper;

    // Transaction
    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public Transaction getTransaction(UUID id) {
            return transactionRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Transaction not Found by id: "+ id));
    }

    // Card Funding Transaction
    public CardFundingTransaction saveCardFundingTransaction(CardFundingTransaction cardFundingTransaction) {
        return cardFundingTransactionRepository.save(cardFundingTransaction);
    }

    public CardFundingTransaction getCardFundingTransaction(UUID id) {
        return cardFundingTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card Funding Transaction not Found by id: "+ id));
    }

    @Transactional
    public void setFailedCardFundingTransaction(CardFundingTransaction cardFundingTransaction,
                                                Transaction transaction, ErrorCode errorCode) {
        cardFundingTransaction.setStatus(TransactionStatus.FAILED);
        saveCardFundingTransaction(cardFundingTransaction);
        transaction.setErrorCode(errorCode);
        transaction.setStatus(TransactionStatus.FAILED);
        saveTransaction(transaction);
    }

    @Transactional
    public void setSuccessfulCardFundingTransaction(CardFundingTransaction cardFundingTransaction,
                                                    Transaction transaction) {
        cardFundingTransaction.setStatus(TransactionStatus.SUCCESSFUL);
        transaction.setStatus(TransactionStatus.SUCCESSFUL);
        saveCardFundingTransaction(cardFundingTransaction);
        saveTransaction(transaction);
    }

    // P2P Transaction
    public P2PTransaction saveP2PTransaction(P2PTransaction p2pTransaction) {
        return p2pTransactionRepository.save(p2pTransaction);
    }

    @Transactional
    public void setFailedP2PTransaction(P2PTransaction p2pTransaction,
                                 Transaction transaction, ErrorCode errorCode) {
        p2pTransaction.setStatus(TransactionStatus.FAILED);
        p2pTransactionRepository.save(p2pTransaction);
        transaction.setErrorCode(errorCode);
        transaction.setStatus(TransactionStatus.FAILED);
        saveTransaction(transaction);
    }

    @Transactional
    public void setSuccessfulP2PTransaction(P2PTransaction p2pTransaction, Transaction transaction) {
        p2pTransaction.setStatus(TransactionStatus.SUCCESSFUL);
        p2pTransactionRepository.save(p2pTransaction);
        transaction.setStatus(TransactionStatus.SUCCESSFUL);
        saveTransaction(transaction);
    }

}
