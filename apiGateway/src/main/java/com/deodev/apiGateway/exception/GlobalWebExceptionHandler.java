package com.deodev.apiGateway.exception;

import com.deodev.apiGateway.dto.ApiResponse;
import com.deodev.apiGateway.dto.ErrorResponse;
import com.deodev.apiGateway.enums.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Order(-2)
@RequiredArgsConstructor
public class GlobalWebExceptionHandler implements WebExceptionHandler {
    private final ObjectMapper mapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        ServerHttpRequest request = exchange.getRequest();

        String uri = request.getURI().toString();

       if (response.isCommitted()) {
           return Mono.error(ex);
       }

       ApiResponse<ErrorResponse> errorResponse = determineResponse(ex, uri);
       response.setStatusCode(HttpStatusCode.valueOf(errorResponse.getStatusCode()));
       response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
       DataBufferFactory dataBufferFactory = response.bufferFactory();
       String errorResponseJson = changeToJsonString(errorResponse);

       return Mono.just(
               dataBufferFactory.wrap(errorResponseJson.getBytes(StandardCharsets.UTF_8))
       ).then();
    }

    ApiResponse<ErrorResponse> determineResponse(Throwable ex, String uri) {
        if (ex instanceof TokenValidationException tve) {
            return buildResponse(HttpStatus.UNAUTHORIZED, uri, tve.getMessage(), ErrorCode.INVALID_TOKEN);
        } else if (ex instanceof ResponseStatusException rse) {
            return buildResponse(rse.getStatusCode(), uri, rse.getReason(), ErrorCode.SYSTEM_ERROR);
        } else {
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, uri, ex.getMessage(), ErrorCode.SYSTEM_ERROR);
        }
    }

    ApiResponse<ErrorResponse> buildResponse(HttpStatusCode status, String path, String message, ErrorCode errorCode) {
        return ApiResponse.error(
                status.value(),
                errorCode == null ? ErrorCode.SYSTEM_ERROR : errorCode,
                ErrorResponse.builder()
                        .message(message)
                        .path(path)
                        .build()
        );
    }

    String changeToJsonString(ApiResponse<?> response) {
        try {
            return mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
