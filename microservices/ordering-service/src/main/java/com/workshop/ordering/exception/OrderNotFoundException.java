package com.workshop.ordering.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Order " + id + " was not found");
    }
}
