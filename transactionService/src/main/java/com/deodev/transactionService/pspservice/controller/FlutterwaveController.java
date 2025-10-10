package com.deodev.transactionService.pspservice.controller;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.pspservice.service.FlutterwaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/psp/flutterwave")
public class FlutterwaveController {

    private final FlutterwaveService flutterwaveService;

    @GetMapping("/card-type/{bin}")
    public ResponseEntity<ApiResponse<?>> getCardType(@PathVariable String bin) {
        ApiResponse<?> response = flutterwaveService.getCardType(bin);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
