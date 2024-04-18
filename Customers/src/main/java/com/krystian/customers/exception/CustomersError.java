package com.krystian.customers.exception;

public enum CustomersError {


    CUSTOMERS_NOT_FOUND("Customers does not exists"),
    CUSTOMERS_PARTICIPANTS_LIMIT_IS_EXCEEDED("Customers participants limits is max"),
    CUSTOMERS_CAN_NOT_SET_FULL_STATUS("Customers can not full status"),
    CUSTOMERS_CAN_NOT_SET_ACTIVE_STATUS("Customers can not set active status"),
    CUSTOMERS_EMAIL_ALREDY_EXISTS("Customers email alredy exists"),
    CUSTOMERS_IS_NOT_ACTIVE("The Customers has maximum number of books"),
    CUSTOMERS_ALREADY_ENROLLED("Customers already enrolled"),
    BOOKS_IS_NOT_ACTIVE("Books is not active"),
    CUSTOMERS_IS_INACTIVE("Customers is inactive"),
    BOOKS_ALREADY_ENROLLED("Books already enrolled"),
    BOOKS_ALREADY_RESERVED("Books already reserved"),
    BOOK_NOT_FOUND("Books not found"),
    CUSTOMERS_ALREADY_RESERVED("Books already reserved"),
    CUSTOMERS_ALREADY_BLOCKED("Customer already blocked.");


    private String message;

    CustomersError(String messgae) {
        this.message = messgae;
    }

    public String getMessage() {
        return message;
    }


}
