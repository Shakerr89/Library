package com.krystian.Books.exception;

public class BooksException extends RuntimeException{

    private BooksError booksError;

    public BooksException(BooksError booksError){this.booksError = booksError;}

    public BooksError getBooksError(){return booksError;}
}
