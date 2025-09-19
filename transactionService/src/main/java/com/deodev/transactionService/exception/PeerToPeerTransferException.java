package com.deodev.transactionService.exception;

public class PeerToPeerTransferException extends RuntimeException {
  public PeerToPeerTransferException(String message, Throwable cause) {
    super(message, cause);
  }

  public PeerToPeerTransferException(String message) {
    super(message);
  }

  public PeerToPeerTransferException(Throwable cause) {
    super(cause);
  }
}
