// package com.example.miniwallet.exception;
package com.example.miniwallet.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String msg) { super(msg); }
}
