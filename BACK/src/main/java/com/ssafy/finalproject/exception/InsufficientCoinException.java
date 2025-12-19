package com.ssafy.finalproject.exception;

public class InsufficientCoinException extends CustomException {
    public InsufficientCoinException(String message) {
        super("INSUFFICIENT_COIN", message);
    }
}
