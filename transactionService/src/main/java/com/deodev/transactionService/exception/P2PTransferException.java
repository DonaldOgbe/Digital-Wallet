package com.deodev.transactionService.exception;

import com.deodev.transactionService.enums.ErrorCode;
import lombok.Getter;

@Getter
public class P2PTransferException extends RuntimeException {
  private final ErrorCode errorCode = ErrorCode.P2P_TRANSFER_ERROR;
  public P2PTransferException(String message, Throwable cause) {
    super(message, cause);
  }

  public P2PTransferException(String message) {
    super(message);
  }

  public P2PTransferException(Throwable cause) {
    super(cause);
  }
}
