package com.krystian.customers.exception;

import feign.FeignException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomersExceptionHandler {

    @ExceptionHandler(CustomersException.class)
    public ResponseEntity<ErrorInfo> handleException(CustomersException e) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        if (CustomersError.CUSTOMERS_NOT_FOUND.equals(e.getCustomersError())) {
            httpStatus = HttpStatus.NOT_FOUND;
        } else if (CustomersError.CUSTOMERS_IS_NOT_ACTIVE.equals(e.getCustomersError())
                || CustomersError.BOOKS_IS_NOT_ACTIVE.equals(e.getCustomersError())) {
            httpStatus = HttpStatus.BAD_REQUEST;
        } else if (CustomersError.CUSTOMERS_PARTICIPANTS_LIMIT_IS_EXCEEDED.equals(e.getCustomersError())
                || CustomersError.CUSTOMERS_CAN_NOT_SET_FULL_STATUS.equals(e.getCustomersError())
                || CustomersError.CUSTOMERS_CAN_NOT_SET_ACTIVE_STATUS.equals(e.getCustomersError())
                || CustomersError.BOOKS_ALREADY_ENROLLED.equals(e.getCustomersError())
                || CustomersError.CUSTOMERS_IS_INACTIVE.equals(e.getCustomersError())) {
            httpStatus = HttpStatus.CONFLICT;
        }
        return ResponseEntity.status(httpStatus).body(new ErrorInfo(e.getCustomersError().getMessage()));
    }


    @ExceptionHandler(FeignException.class)
    public ResponseEntity<?> handleFeignException(FeignException e) {
        return ResponseEntity.status(e.status()).body(new JSONObject(e.contentUTF8()).toMap());
    }


}
