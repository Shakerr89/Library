package com.krystian.customers.exception;

public class CustomersException extends RuntimeException {

    private CustomersError customersError;

    public CustomersException(CustomersError customersError) {
        this.customersError = customersError;
    }


    public CustomersError getCustomersError() {
        return customersError;
    }


}
