package com.krystian.customers.service;

import com.krystian.customers.model.dto.BooksDto;
import com.krystian.customers.model.Customers;

import java.util.List;

public interface CustomersService {

    List<Customers> getCustomers(Customers.Status status);

    Customers getCustomers(String code);

    Customers addCustomers(Customers customers);
    void deleteCustomers(String code);

    Customers putCustomers(String code, Customers customers);

    Customers pathCustomers(String code, Customers customers);


    List<BooksDto> getCustomersBooks(String customerCode);

    void customersEnrollment(String customerCode,Long bookCode);

    void deleteCustomersBookByTitle(String customerCode, Long bookCode);

    void deactiveCustomer(String customerCode);

    void reservedBook(String customerCode, Long bookCode);

    void deleteReserveBook(String customerCode, Long bookCode);

    void extendReturnDate(String customerCode, Long bookCode);










}
