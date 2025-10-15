package com.deodev.transactionService.pspService.flutterwave.controller;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.pspService.flutterwave.dto.request.InitiateChargeCardRequest;
import com.deodev.transactionService.pspService.flutterwave.service.FlutterwaveCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/psp/flutterwave")
public class FlutterwaveController {

    private final FlutterwaveCardService flutterwaveCardService;

    @GetMapping("/card/card-type/{bin}")
    public ResponseEntity<ApiResponse<?>> getCardType(@PathVariable String bin) {
        ApiResponse<?> response = flutterwaveCardService.getCardType(bin);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/card/initiate-card-funding")
    public ResponseEntity<ApiResponse<?>> initiateCardFunding(@Valid @RequestBody InitiateChargeCardRequest request,
                                                              @RequestHeader("X-User-Id") String userId,
                                                              @RequestHeader("Idempotency-Key") String idempotencyKey) throws Exception {
        ApiResponse<?> response = flutterwaveCardService.initiateChargeCard(request, userId, idempotencyKey);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
