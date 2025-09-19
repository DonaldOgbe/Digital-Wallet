package com.deodev.transactionService.transactionService.service;

import com.deodev.transactionService.client.WalletServiceClient;
import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.dto.request.ReserveFundsRequest;
import com.deodev.transactionService.dto.request.TransferFundsRequest;
import com.deodev.transactionService.dto.response.ReserveFundsResponse;
import com.deodev.transactionService.dto.response.TransferFundsResponse;
import com.deodev.transactionService.enums.ErrorCode;
import com.deodev.transactionService.enums.TransactionStatus;
import com.deodev.transactionService.exception.ExternalServiceException;
import com.deodev.transactionService.exception.PeerToPeerTransferException;
import com.deodev.transactionService.transactionService.dto.request.P2PTransferRequest;
import com.deodev.transactionService.transactionService.dto.response.P2PTransferResponse;
import com.deodev.transactionService.transactionService.model.Transaction;
import com.deodev.transactionService.transactionService.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public P2PTransferResponse processP2PTransfer(P2PTransferRequest request, String jwt) {
        Transaction transaction =
                createNewTransaction(request.senderAccountNumber(), request.receiverAccountNumber(), request.amount());

        try {
            validatePin(request.pin(), jwt, transaction);

            UUID reservationId = reserveFunds(
                    ReserveFundsRequest.builder()
                            .accountNumber(request.senderAccountNumber()).amount(request.amount()).transactionId(transaction.getId())
                            .build(), jwt, transaction);

            UUID transactionId = transferFunds(
                    TransferFundsRequest.builder()
                            .accountNumber(request.receiverAccountNumber()).transactionId(transaction.getId())
                            .build(), jwt, transaction);

            return P2PTransferResponse.builder()
                    .transactionId(transactionId)
                    .senderAccountNumber(request.senderAccountNumber())
                    .receiverAccountNumber(request.receiverAccountNumber())
                    .amount(request.amount())
                    .build();

        } catch (PeerToPeerTransferException e) {
            throw new PeerToPeerTransferException(e.getMessage(), e);
        } catch (ExternalServiceException e) {
            throw new ExternalServiceException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    void validatePin(String pin, String jwt, Transaction transaction) {
        ApiResponse<?> response = getResponseFromWalletClient(() -> walletServiceClient.validatePin(jwt, pin));

        if (!response.isSuccess()) {
            transaction.setErrorCode(ErrorCode.PIN_MISMATCH);
            setFailedTransaction(transaction);
            throw new PeerToPeerTransferException("Invalid Pin");
        }
    }

    UUID reserveFunds(ReserveFundsRequest request, String jwt, Transaction transaction) {
        ApiResponse<?> response = getResponseFromWalletClient(() -> walletServiceClient.reserveFunds(jwt, request));

        if(!response.isSuccess()) {
            transaction.setErrorCode(ErrorCode.FUND_RESERVATION_ERROR);
            setFailedTransaction(transaction);
            throw new PeerToPeerTransferException("Failed to reserve funds");
        }

        ReserveFundsResponse data = (ReserveFundsResponse) response.getData();
        return data.fundReservationId();
    }

    UUID transferFunds(TransferFundsRequest request, String jwt, Transaction transaction) {
        ApiResponse<?> response = getResponseFromWalletClient(() -> walletServiceClient.transferFunds(jwt, request));

        if(!response.isSuccess()) {
            transaction.setErrorCode(ErrorCode.P2P_TRANSFER_ERROR);
            setFailedTransaction(transaction);
            throw new PeerToPeerTransferException("Failed to transfer funds");
        }

        TransferFundsResponse data = (TransferFundsResponse) response.getData();
        return data.transactionId();
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
