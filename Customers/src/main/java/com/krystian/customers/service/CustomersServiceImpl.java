package com.krystian.customers.service;


import com.krystian.customers.exception.CustomersError;
import com.krystian.customers.exception.CustomersException;
import com.krystian.customers.model.Customers;
import com.krystian.customers.model.CustomersBooks;
import com.krystian.customers.model.CustomersReservedBooks;
import com.krystian.customers.model.dto.BooksDto;
import com.krystian.customers.model.dto.NotificationInfoDto;
import com.krystian.customers.repository.CustomersRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Component
@Slf4j
public class CustomersServiceImpl implements CustomersService {

    private static final String REMINDER_MESSAGE = "reminderMessage";
    private static final String SEND_NOTIFICATION = "notificationQueue";
    private static final String TEST = "test";

/*
    private static final String EXCHANGE_ENROLL_FINISH = "addBooksToCustomer";
    private static final String DEACTIVE_CUSTOMER = "deactive";
    private static final String NEW_CUSTOMER = "newCustomer";
    private static final String DELETE_CUSTOMER = "deleteCustomer";
    private static final String DELETE_CUSTOMERS_BOOK = "deleteCustomersBook";
    private static final String RESERVED_BOOKS = "reservedBooks";
    private static final String UNRESERVED_BOOKS = "unReservedBooks";
    private static final String EXTEND_RETURN_DATE = "extendReturnDate";
*/ // <- tutaj jest lista kolejek na Rabbita przed zmianami.


    private final CustomersRepository customersRepository;
    private final BooksServiceClient booksServiceClient;
    private final RabbitTemplate rabbitTemplate;


    @Autowired
    public CustomersServiceImpl(CustomersRepository customersRepository, BooksServiceClient booksServiceClient, RabbitTemplate rabbitTemplate) {
        this.customersRepository = customersRepository;
        this.booksServiceClient = booksServiceClient;
        this.rabbitTemplate = rabbitTemplate;

    }

    public List<Customers> getCustomers(Customers.Status status) {
        if (status != null) {
            return customersRepository.findAllByStatus(status);
        }
        return customersRepository.findAll();
    }

    public Customers getCustomers(String code) {
        return customersRepository.findById(code)
                .orElseThrow(() -> new CustomersException(CustomersError.CUSTOMERS_NOT_FOUND));
    }


    public Customers addCustomers(Customers customers) {
        customers.validateCustomers();
        validateCustomersEmailExists(customers);
        newCustomerRabbitMq(customers);
        return customersRepository.save(customers);

    }

    private void newCustomerRabbitMq(final Customers customers) {
        NotificationInfoDto notificationInfo = createNotificationInfo(customers, "NEW_CUSTOMER");
        rabbitTemplate.convertAndSend(SEND_NOTIFICATION, notificationInfo);
    }

    public void deleteCustomers(String code) {
        Customers customers = customersRepository.findById(code)
                .orElseThrow(() -> new CustomersException(CustomersError.CUSTOMERS_NOT_FOUND));

        customersRepository.delete(customers);
        deleteCustomerRabbitMq(customers);

    }

    private void deleteCustomerRabbitMq(final Customers customers) {
        NotificationInfoDto notificationInfo = createNotificationInfo(customers, "DELETE_CUSTOMER");
        rabbitTemplate.convertAndSend(SEND_NOTIFICATION, notificationInfo);
    }

    public Customers putCustomers(String code, Customers customers) {
        validateCustomersEmailExists(customers);
        return customersRepository.findById(code)
                .map(customerFromDb -> {
                    if (customersRepository.existsByEmail(customers.getEmail()) &&
                            !customerFromDb.getEmail().equals(customers.getEmail())
                    ) {
                        throw new CustomersException(CustomersError.CUSTOMERS_EMAIL_ALREDY_EXISTS);
                    }
                    customerFromDb.setCode(customers.getCode());
                    customerFromDb.setFirstName(customers.getFirstName());
                    customerFromDb.setLastName(customers.getLastName());
                    customerFromDb.setEmail(customers.getEmail());
                    customerFromDb.setStatus(customers.getStatus());
                    customerFromDb.setParticipantsLimit(customers.getParticipantsLimit());
                    customerFromDb.setParticipantsNumber(customers.getParticipantsNumber());
                    return customersRepository.save(customerFromDb);
                }).orElseThrow(() -> new CustomersException(CustomersError.CUSTOMERS_NOT_FOUND));
    }

    public Customers pathCustomers(String code, Customers customers) {
        validateCustomersEmailExists(customers);
        return customersRepository.findById(code)
                .map(customerFromDb -> {
                    if (!StringUtils.isEmpty(customers.getFirstName())) {
                        customerFromDb.setFirstName(customers.getFirstName());
                    }
                    if (!StringUtils.isEmpty(customers.getLastName())) {
                        customerFromDb.setLastName(customers.getLastName());
                    }
                    if (!StringUtils.isEmpty(customers.getLastName())) {
                        customerFromDb.setLastName(customers.getLastName());
                    }
                    if (!StringUtils.isEmpty(customers.getEmail())) {
                        customerFromDb.setEmail(customers.getEmail());
                    }
                    if (!StringUtils.isEmpty(customers.getStatus())) {
                        customerFromDb.setStatus(customers.getStatus());
                    }
                    return customersRepository.save(customerFromDb);
                }).orElseThrow(() -> new CustomersException(CustomersError.CUSTOMERS_NOT_FOUND));
    }

    public List<BooksDto> getCustomersBooks(String customerCode) {
        Customers customers = getCustomers(customerCode);
        List<@NotNull String> titleBook = getCustomersBooksTitle(customers);
        return booksServiceClient.getBooksByTitle(titleBook);
    }

//          -------------------------------------------------------------------------           //

    public void customersEnrollment(String customerCode, Long bookCode) {
        Customers customers = getCustomers(customerCode);
        ValidateCustomersCode(customers);
        BooksDto booksDto = booksServiceClient.getBooksById(bookCode);
        if (customers.getStatus() == Customers.Status.BLOCKED) {
            throw new CustomersException(CustomersError.CUSTOMERS_ALREADY_BLOCKED);

        }
        if (customers.getCustomersBooks().stream().anyMatch(books -> booksDto.getTitle().equals(books.getTitle()))) {
            throw new CustomersException(CustomersError.BOOKS_ALREADY_ENROLLED);
        }
        validateBooksBeforeCustomerEnrollment(booksDto, customers);
        booksServiceClient.updateBookCode(bookCode, BooksDto.Status.NOTAVAILABLE);
        customers.incrementParticiapantsNumber();
        LocalDateTime returnDate = LocalDateTime.now().plusMinutes(1);
        //LocalDateTime returnDate = LocalDateTime.now().plusDays(7);
        customers.getCustomersBooks().add(new CustomersBooks(booksDto.getTitle(), bookCode));
        customersRepository.save(customers);
        enrollFinishRabbitMq(customers, booksDto.getTitle(), returnDate);

    }

    private void enrollFinishRabbitMq(final Customers customers, final String bookTitle, final LocalDateTime returnDate) {
        NotificationInfoDto notificationInfo = createNotificationInfoWithBookTitle(customers, bookTitle, "EXCHANGE_ENROLL_FINISH");
        notificationInfo.setReturnDate(returnDate);
        rabbitTemplate.convertAndSend(SEND_NOTIFICATION, notificationInfo);

    }

    public void extendReturnDate(final String customerCode, final Long bookCode) {
        Customers customers = getCustomers(customerCode);
        BooksDto booksDto = booksServiceClient.getBooksById(bookCode);
        for (CustomersBooks book : customers.getCustomersBooks()) {
            if (book.getBookCode().equals(bookCode)) {
                LocalDateTime returnDate = book.getReturnDateInStudent();
                LocalDateTime extendDate = returnDate.plusDays(2);
                book.setReturnDateInStudent(extendDate);
                customersRepository.save(customers);
                extendReturnDateRabbitMq(customers, booksDto.getTitle(), extendDate);
                return;
            }
        }
        throw new CustomersException(CustomersError.BOOK_NOT_FOUND);
    }

    private void extendReturnDateRabbitMq(final Customers customers, final String bookTitle, final LocalDateTime extendDate) {
        NotificationInfoDto notificationInfoDto = createNotificationInfoWithBookTitle(customers, bookTitle, "EXTEND_RETURN_DATE");
        notificationInfoDto.setReturnDate(extendDate);
        rabbitTemplate.convertAndSend(SEND_NOTIFICATION, notificationInfoDto);

    }
//          -------------------------------------------------------------------------           //

    public void reservedBook(String customerCode, Long bookCode) {
        Customers customers = customersRepository.findById(customerCode)
                .orElseThrow(() -> new CustomersException(CustomersError.CUSTOMERS_NOT_FOUND));
        BooksDto booksDto = booksServiceClient.getBooksById(bookCode);
        //Sprawdzamy czy książka jest zarezerwowana przez klienta
        if (customers.getCustomerReservedBooks().stream().anyMatch(books -> booksDto.getTitle().equals(books.getTitle()))) {
            throw new CustomersException(CustomersError.CUSTOMERS_ALREADY_RESERVED);
        }// sprawdzamy czy ksiazka jest dostepna
        if (booksDto.getStatus().equals(BooksDto.Status.NOTAVAILABLE))
            throw new CustomersException(CustomersError.BOOKS_IS_NOT_ACTIVE);
        LocalDateTime returnDate = LocalDateTime.now().plusDays(2);
        CustomersReservedBooks customersReservedBooks = new CustomersReservedBooks(booksDto.getStatus(), bookCode, booksDto.getTitle());
        customers.getCustomerReservedBooks().add(customersReservedBooks);
        customers.incrementParticiapantsNumber();
        customersReservedBooks.setStatus(CustomersReservedBooks.Status.RESERVED);
        customersRepository.save(customers);
        booksServiceClient.updateBookCode(bookCode, BooksDto.Status.RESERVED);
        reserveBooksRabbitMq(customers, booksDto.getTitle(), returnDate);

    }

    private void reserveBooksRabbitMq(final Customers customers, final String bookTitle, final LocalDateTime returnDate) {
        NotificationInfoDto notificationInfo = createNotificationInfoWithBookTitle(customers, bookTitle, "RESERVED");
        notificationInfo.setReturnDate(returnDate);
        rabbitTemplate.convertAndSend(SEND_NOTIFICATION, notificationInfo);
    }

//          -------------------------------------------------------------------------           //

    public void deleteReserveBook(String customerCode, Long bookCode) {
        Customers customers = getCustomers(customerCode);
        BooksDto booksDto = booksServiceClient.getBooksById(bookCode);
        CustomersReservedBooks bookToRemoveFromReserve = null;
        for (CustomersReservedBooks book : customers.getCustomerReservedBooks()) {
            if (book.getBookCode().equals(bookCode)) { // Sprawdź, czy bookCode jest typu Long
                bookToRemoveFromReserve = book;
                break;
            }
        }
        if (bookToRemoveFromReserve != null) {
            customers.getCustomerReservedBooks().remove(bookToRemoveFromReserve);
            customers.decrementParticipantsNumber();
            booksServiceClient.updateBookCode(bookCode, BooksDto.Status.AVAILABLE);
            customersRepository.save(customers);
            NotificationInfoDto notificationInfo = createNotificationInfoWithBookTitle(customers, booksDto.getTitle(), "UNRESERVED_BOOKS");
            deleteReserveBooksRabbitMq(notificationInfo);
        } else {
            throw new CustomersException(CustomersError.BOOK_NOT_FOUND);
        }
        if (customers.getStatus().equals(Customers.Status.FULL)) {
            customers.setStatus(Customers.Status.ACTIVE);
            customersRepository.save(customers);
        }
    }


    private void deleteReserveBooksRabbitMq(NotificationInfoDto notificationInfo) {
        rabbitTemplate.convertAndSend(SEND_NOTIFICATION, notificationInfo);
    }

//          -------------------------------------------------------------------------           //

    public void deleteCustomersBookByTitle(String customerCode, Long bookCode) {
        Customers customers = getCustomers(customerCode);
        BooksDto booksDto = booksServiceClient.getBooksById(bookCode);
        CustomersBooks bookToRemove = null;
        for (CustomersBooks book : customers.getCustomersBooks()) {
            if (book.getBookCode().equals(bookCode)) {
                bookToRemove = book;
                break;
            }
        }
        if (bookToRemove != null) {
            customers.getCustomersBooks().remove(bookToRemove);
            customers.decrementParticipantsNumber();
            booksServiceClient.updateBookCode(bookCode, BooksDto.Status.AVAILABLE);
            NotificationInfoDto notificationInfo = createNotificationInfoWithBookTitle(customers, booksDto.getTitle(), "DELETE_CUSTOMERS_BOOK");
            deleteCustomersBooksRabbitMq(notificationInfo);
            customers.setBlockedExpirationDate(null);  //<- tutaj trzeba wymyślić tak, żeby po usunięciu ksiązki po terminie zmieniał się ten status.
            customersRepository.save(customers);
        } else {
            throw new CustomersException(CustomersError.BOOK_NOT_FOUND);

        }
        if (customers.getStatus().equals(Customers.Status.FULL)) {
            customers.setStatus(Customers.Status.ACTIVE);
            customersRepository.save(customers);
        }
    }

    private void deleteCustomersBooksRabbitMq(NotificationInfoDto notificationInfo) {
        rabbitTemplate.convertAndSend(SEND_NOTIFICATION, notificationInfo);
    }

//          -------------------------------------------------------------------------           //

    // deaktywacja customera.

    public void deactiveCustomer(final String customerCode) {
        Customers customers = getCustomers(customerCode);
        ValidateCustomersCode(customers);
        customers.setStatus(Customers.Status.INACTIVE);
        customersRepository.save(customers);
        deactiveCustomerRabbitMq(customers);
    }

    public void deactiveCustomerRabbitMq(final Customers customers) {
        NotificationInfoDto notificationInfo = createNotificationInfo(customers, "DEACTIVE_CUSTOMER");
        rabbitTemplate.convertAndSend(SEND_NOTIFICATION, notificationInfo);
    }

    //          -------------------------------------------------------------------------           //

    @Scheduled(cron = "*/13 * * * * *") // Ustawienia częstotliwości odświeżania metody.
    public void checkBlockedCustomers() {
        LocalDateTime now = LocalDateTime.now();
        List<Customers> blockedCustomers = customersRepository.findAllByStatus(Customers.Status.BLOCKED);
        for (Customers customer : blockedCustomers) {
            if (customer.isSendNotificationBlockedCustomer() && customer.getBlockedExpirationDate() == null) { // Dodany warunek sprawdzający flagę
                //LocalDateTime expirationDate = customer.getBlockedExpirationDate();
                //if (expirationDate != null && now.isAfter(expirationDate)) {
                    if (customer.getStatus() == Customers.Status.BLOCKED) {
                        customer.setStatus(Customers.Status.ACTIVE);
                        customer.setSendNotificationBlockedCustomer(false);
                        customer.setBlockedExpirationDate(null);
                        customersRepository.save(customer);
                        NotificationInfoDto notificationInfoDto = createNotificationInfo(customer, "UNBLOCK_CUSTOMER");
                        checkBlockedCustomersRabbitMq(notificationInfoDto);
                    }
                }
            }
        }
   // }

    private void checkBlockedCustomersRabbitMq(final NotificationInfoDto notificationInfoDto) {
        rabbitTemplate.convertAndSend(SEND_NOTIFICATION, notificationInfoDto);
    }


    @Scheduled(cron = "*/10 * * * * *")
    public void blockCustomersIfReturnDateExpired() {
        List<Customers> allCustomers = customersRepository.findAll();
        for (Customers customer : allCustomers) {
            if (!customer.isSendNotificationBlockedCustomer()) {
                for (CustomersBooks books : customer.getCustomersBooks()) {
                    LocalDateTime returnDate = books.getReturnDateInStudent();
                    if (returnDate != null && LocalDateTime.now().isAfter(returnDate)){
                        NotificationInfoDto notificationInfoDto = createNotificationInfoWithBookTitle(customer, books.getTitle(), "BLOCK_CUSTOMER");
                        customer.setStatus(Customers.Status.BLOCKED);
                        setBlockedExpirationDate(customer);
                        customer.setSendNotificationBlockedCustomer(true); // Ustawienie flagi na true, aby zaznaczyć, że klient został powiadomiony
                        customersRepository.save(customer);
                        blockCustomersIfReturnDateExpiredRabbitMq(notificationInfoDto);
                        break; // Nie trzeba dalej sprawdzać, jeśli klient jest już zablokowany
                    }
                }
            }
        }
    }


    private void blockCustomersIfReturnDateExpiredRabbitMq(NotificationInfoDto notificationInfoDto) {
        rabbitTemplate.convertAndSend(SEND_NOTIFICATION, notificationInfoDto);
    }


    @Scheduled(cron = "0 0 * * * *")
    public void sendReminderMessages() {
        LocalDateTime now = LocalDateTime.now();
        List<Customers> customersWithReturnDate = customersRepository.findAllByStatus(Customers.Status.ACTIVE);
        for (Customers customer : customersWithReturnDate) {
            for (CustomersBooks book : customer.getCustomersBooks()) {
                if (book.getReturnDateInStudent() != null && book.getReturnDateInStudent().toLocalDate().isEqual(now.toLocalDate().plusDays(1)) && !book.isReminderSent()) {
                    NotificationInfoDto notificationInfo = createNotificationInfo2(customer);
                    notificationInfo.setBookTitle(book.getTitle());
                    notificationInfo.setCustomerEmail(customer.getEmail());
                    notificationInfo.setReturnDate(book.getReturnDateInStudent());
                    rabbitTemplate.convertAndSend(REMINDER_MESSAGE, notificationInfo);
                    book.setReminderSent(true); // Ustawienie flagi reminderSent na true
                }
            }
            customersRepository.save(customer); // Zapisanie zmian w bazie danych po przetworzeniu wszystkich książek klienta
        }
    }

//          -------------------------------------------------------------------------           //

    // To jest metoda ktora pobiera nam daje o books i customers
    private static NotificationInfoDto createNotificationInfo(final Customers customers, final String notificationType) {
        List<@NotNull String> customersBooks = getCustomersBooksTitle(customers);
        NotificationInfoDto notificationInfo = NotificationInfoDto.builder()
                .customerCode(customers.getCode())
                .customerFirstName(customers.getFirstName())
                .customerLastName(customers.getLastName())
                .customerEmail(customers.getEmail())
                .books(customersBooks)
                .notificationType(notificationType)
                .blockedExpirationDate(customers.blockedExpirationDate)
                .build();

        return notificationInfo;
    }

    private NotificationInfoDto createNotificationInfo2(Customers customers) {
        List<String> customersBooks = customers.getCustomersBooks().stream()
                .map(CustomersBooks::getTitle)
                .collect(Collectors.toList());
        return NotificationInfoDto.builder()
                .customerCode(customers.getCode())
                .customerFirstName(customers.getFirstName())
                .customerLastName(customers.getLastName())
                .customerEmail(customers.getEmail())
                .books(customersBooks)
                .build();
    }

    private NotificationInfoDto createNotificationInfoWithBookTitle(final Customers customers, final String bookTitle, final String notificationType) {
        List<@NotNull String> customersBooks = getCustomersBooksTitle(customers);
        NotificationInfoDto notificationInfo = NotificationInfoDto.builder()
                .customerCode(customers.getCode())
                .customerFirstName(customers.getFirstName())
                .customerLastName(customers.getLastName())
                .customerEmail(customers.getEmail())
                .books(customersBooks)
                .bookTitle(bookTitle)
                .notificationType(notificationType)
                .blockedExpirationDate(customers.blockedExpirationDate)
                .build();
        return notificationInfo;
    }

//          -------------------------------------------------------------------------           //

    private static void validateBooksBeforeCustomerEnrollment(final BooksDto booksDto, Customers customers) {
        if (!BooksDto.Status.AVAILABLE.equals(booksDto.getStatus())) {
            throw new CustomersException(CustomersError.BOOKS_IS_NOT_ACTIVE);
        }
        if (customers.getCustomersBooks().stream()
                .anyMatch(books -> booksDto.getTitle().equals(books.getTitle()))) {
            throw new CustomersException(CustomersError.BOOKS_ALREADY_ENROLLED);
        }
    }

    private static void ValidateCustomersCode(Customers customers) {
        if (Customers.Status.INACTIVE.equals(customers.getStatus())) {
            throw new CustomersException(CustomersError.CUSTOMERS_IS_NOT_ACTIVE);

        } else if (Customers.Status.BLOCKED.equals(customers.getStatus())) {
            throw new CustomersException(CustomersError.CUSTOMERS_ALREADY_BLOCKED);
        }
    }

    private static List<@NotNull String> getCustomersBooksTitle(Customers customers) {
        List<@NotNull String> titleMembers = customers.getCustomersBooks().stream()
                .map(CustomersBooks::getTitle).collect(Collectors.toList());
        return titleMembers;
    }

    private void validateCustomersEmailExists(Customers customers) {
        if (customersRepository.existsByEmail(customers.getEmail())) {
            throw new CustomersException(CustomersError.CUSTOMERS_EMAIL_ALREDY_EXISTS);
        }
    }

    public void setBlockedExpirationDate(Customers customers) {
        LocalDateTime expirationDate = LocalDateTime.now().plusMinutes(1);
        customers.setBlockedExpirationDate(expirationDate);
        customersRepository.save(customers);
    }


}
