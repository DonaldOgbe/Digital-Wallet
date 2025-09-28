package com.deodev.transactionService.transactionService.service;

import com.deodev.transactionService.client.WalletServiceClient;
import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.dto.request.ReserveFundsRequest;
import com.deodev.transactionService.dto.response.ReserveFundsResponse;
import com.deodev.transactionService.enums.ErrorCode;
import com.deodev.transactionService.enums.TransactionStatus;
import com.deodev.transactionService.exception.ExternalServiceException;
import com.deodev.transactionService.exception.P2PTransferException;
import com.deodev.transactionService.exception.PinMismatchException;
import com.deodev.transactionService.exception.ResourceNotFoundException;
import com.deodev.transactionService.transactionService.dto.request.P2PTransferRequest;
import com.deodev.transactionService.transactionService.dto.response.P2PTransferResponse;
import com.deodev.transactionService.transactionService.model.Transaction;
import com.deodev.transactionService.transactionService.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletServiceClient walletServiceClient;

    public Transaction createNewTransaction(String sender, String receiver, Long amount) {
        Transaction transaction = Transaction.builder()
                .senderAccountNumber(sender)
                .receiverAccountNumber(receiver)
                .amount(amount)
                .build();

        return transactionRepository.save(transaction);
    }

    public ApiResponse<?> processP2PTransfer(P2PTransferRequest request, String userId) {
        Transaction transaction = Transaction.builder()
                .senderAccountNumber(request.senderAccountNumber())
                .receiverAccountNumber(request.receiverAccountNumber())
                .amount(request.amount())
                .build();

        try {





        } catch (P2PTransferException e) {
            throw new P2PTransferException(e.getMessage(), e);
        } catch (ExternalServiceException e) {
            throw new ExternalServiceException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    UUID validateAndReserveFunds(ReserveFundsRequest request, String userId, Transaction transaction) {
        ApiResponse<?> response = getResponseFromWalletClient(() -> walletServiceClient.reserveFunds(request, userId));

        if(!response.isSuccess()) {
            if (response.getStatusCode() == HttpStatus.BAD_REQUEST.value()) {
                transaction.setErrorCode(ErrorCode.PIN_MISMATCH);
                setFailedTransaction(transaction);
                throw new PinMismatchException("Incorrect Pin");
            }
            transaction.setErrorCode(ErrorCode.FUND_RESERVATION_ERROR);
            setFailedTransaction(transaction);
            throw new P2PTransferException("Failed to reserve funds");
        }

        ReserveFundsResponse data = (ReserveFundsResponse) response.getData();
        return data.fundReservationId();
    }

//    UUID transferFunds(TransferFundsRequest request, String jwt, Transaction transaction) {
//        ApiResponse<?> response = getResponseFromWalletClient(() -> walletServiceClient.transferFunds(jwt, request));
//
//        if(!response.isSuccess()) {
//            transaction.setErrorCode(ErrorCode.P2P_TRANSFER_ERROR);
//            setFailedTransaction(transaction);
//            throw new PeerToPeerTransferException("Failed to transfer funds");
//        }
//
//        TransferFundsResponse data = (TransferFundsResponse) response.getData();
//        return data.transactionId();
//    }

    public Transaction findByIdempotencyKey(String key) {
        return transactionRepository.findByIdempotencyKey(key).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Transaction not found for idempotency key: %s".formatted(key)));
    }

    <T> ApiResponse<T> getResponseFromWalletClient(Supplier<ApiResponse<T>> func) {
        try {
            return func.get();
        } catch (Exception e) {
            throw  new ExternalServiceException(e.getMessage(), e);
        }
    }

    void setFailedTransaction(Transaction transaction) {
        transaction.setStatus(TransactionStatus.FAILED);
        transactionRepository.save(transaction);
    }


}
