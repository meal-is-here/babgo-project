package com.babgo.domain.payment.exception;

import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;

public class PaymentException extends CustomException {

  public PaymentException(ErrorCode errorCode) {
    super(errorCode);
  }

  public PaymentException(ErrorCode errorCode, String customMessage) {
    super(errorCode, customMessage);
  }
}
