package com.krystian.customers.controller;

import com.krystian.customers.model.dto.BooksDto;
import com.krystian.customers.model.Customers;
import com.krystian.customers.service.BooksServiceClient;
import com.krystian.customers.service.CustomersService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomersController {

    private final CustomersService customersService;
    private final BooksServiceClient booksServiceClient;

    public CustomersController(CustomersService customersService, BooksServiceClient booksServiceClient) {
        this.customersService = customersService;
        this.booksServiceClient = booksServiceClient;
    }

    @PostMapping
    public Customers addCustomers(@Valid @RequestBody Customers customers) {
        return customersService.addCustomers(customers);
    }

    @GetMapping
    public List<Customers> getCustomers(@RequestParam(required = false) Customers.Status status) {
        return customersService.getCustomers(status);
    }

    @GetMapping("/{firstName}")
    public Customers getCustomers(@PathVariable String firstName) {
        return customersService.getCustomers(firstName);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<String> deleteCustomers(@PathVariable String code) {
        Customers deleteCustomers = customersService.getCustomers(code);
        String deleteCustomersCode = deleteCustomers.getCode();
        customersService.deleteCustomers(code);
        String responseMessage = "Klient " + deleteCustomersCode + " został pomyślnie usunięty";
        return ResponseEntity.ok(responseMessage);
    }

    @PutMapping("/{code}") // Podmiana calego klienta
    public Customers putCustomers(@PathVariable String code, @RequestBody @Valid Customers customers) {
        return customersService.putCustomers(code, customers);
    }

    @PatchMapping("/{code}") // Podmiana fragmentu danych klienta
    public Customers pathCustomers(@PathVariable String code, @RequestBody @Valid Customers customers) {
        return customersService.pathCustomers(code, customers);
    }

    @GetMapping("/{code}/books")
    public List<BooksDto> getCustomersBooks(@PathVariable String code) {
        return customersService.getCustomersBooks(code);
    }

    // Funkcja poniżej powzala na dodanie książki do klienta. Otrzymujemy również komunikat po poprawnym dodaniu ksiązki do klienta.
    @PostMapping("/{customerCode}/books/{bookCode}")
    public ResponseEntity<?> customerEnrollment(@PathVariable String customerCode, @PathVariable Long bookCode) {
        Customers customersEmrollment = customersService.getCustomers(customerCode);
        String customersName = customersEmrollment.getCode();
        BooksDto booksEnrollment = booksServiceClient.getBooksById(bookCode);
        String booksName = booksEnrollment.getTitle();
        customersService.customersEnrollment(customerCode, bookCode);
        String responeMessage = "Książka " + booksName + " została dodana pomyślnie do klienta " + customersName + ".";
        return ResponseEntity.ok(responeMessage);
    }


    // Ta metoda służy do testowania Feign
    @GetMapping("/test")
    public List<BooksDto> testFeingClient() {
        return booksServiceClient.getBooks();
    }


    @DeleteMapping("/{customerCode}/books/{bookCode}")
    public ResponseEntity<?> deleteCustomersBookByTitle(@PathVariable String customerCode, @PathVariable Long bookCode) {
        customersService.deleteCustomersBookByTitle(customerCode, bookCode);
        BooksDto booksDto = booksServiceClient.getBooksById(bookCode);
        String bookName = booksDto.getTitle();
        String responseMessage = "Książką " + bookName + " o numerze " + bookCode + " została usunięta od użytkownika " + customerCode + ".";
        return ResponseEntity.ok(responseMessage);
    }

    @DeleteMapping("/{customerCode}/books/{bookCode}/reserved")
    public ResponseEntity<?> unReserveBook(@PathVariable String customerCode, @PathVariable Long bookCode) {
        customersService.deleteReserveBook(customerCode, bookCode);
        BooksDto bookUnReserveBook = booksServiceClient.getBooksById(bookCode);
        String bookName = bookUnReserveBook.getTitle();
        String responeMessage = "Rezerwacja książki  " + bookName + " o numerze " + bookCode + " została zakończona.";
        return ResponseEntity.ok(responeMessage);
    }

    @PostMapping("/{code}/deactiveCustomer")
    public ResponseEntity<?> deactiveCustomer(@PathVariable String code) {
        customersService.deactiveCustomer(code);
        String responeMessage = "Klient " + code + " został dezaktywowany.";
        return ResponseEntity.ok(responeMessage);
    }

    @PostMapping("/{customerCode}/books/{bookCode}/reserved")
    private ResponseEntity<?> reserveBook(@PathVariable String customerCode, @PathVariable Long bookCode) {
        customersService.reservedBook(customerCode, bookCode);
        BooksDto booksReserve = booksServiceClient.getBooksById(bookCode);
        String booksName = booksReserve.getTitle();
        String responseMessage = "Książka " + booksName + " o numerze " + bookCode + " została zarezerwowana.";
        return ResponseEntity.ok(responseMessage);

    }

    @PostMapping("/{customerCode}/books/{bookCode}/extend")
    private ResponseEntity<?> extendReservation(@PathVariable String customerCode, @PathVariable Long bookCode) {
        customersService.extendReturnDate(customerCode, bookCode);
        BooksDto extendBook = booksServiceClient.getBooksById(bookCode);
        String bookName = extendBook.getTitle();
        String responseMessage = "Termin oddania książki  " + bookName + " o numerze " + bookCode + " został przedłużony o 2 dni.";
        return ResponseEntity.ok(responseMessage);
    }


}
