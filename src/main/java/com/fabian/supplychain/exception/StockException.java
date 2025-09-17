package com.fabian.supplychain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for insufficient stock.
 * Maps to an HTTP 400 Bad Request status.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StockException extends RuntimeException {
    public StockException(String message) {
        super(message);
    }
}