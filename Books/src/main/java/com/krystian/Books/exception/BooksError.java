package com.krystian.Books.exception;

public enum BooksError {

    BOOKS_NOT_FOUND("Books does not found"),
    BOOKS_ALREADY_EXISTS("Books already exists");



    private String message;

    BooksError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
