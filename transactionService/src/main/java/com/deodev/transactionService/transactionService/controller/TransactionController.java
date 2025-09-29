package com.deodev.transactionService.transactionService.controller;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.enums.Currency;
import com.deodev.transactionService.transactionService.dto.request.P2PTransferRequest;
import com.deodev.transactionService.transactionService.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer/p2p")
    public ResponseEntity<ApiResponse<?>> p2pTransfer(@Valid @RequestBody P2PTransferRequest request,
                                                      @RequestHeader("X-User-Id") String userId,
                                                      @RequestParam("currency")Currency currency) {
        ApiResponse<?> response = transactionService.processP2PTransfer(request, userId, currency);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
