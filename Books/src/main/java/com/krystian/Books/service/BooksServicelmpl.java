package com.krystian.Books.service;

import com.krystian.Books.exception.BooksError;
import com.krystian.Books.exception.BooksException;
import com.krystian.Books.model.Books;
import com.krystian.Books.repository.BooksRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BooksServicelmpl implements BooksService {
    private final BooksRepository booksRepository;

    public BooksServicelmpl(BooksRepository booksRepository) {
        this.booksRepository = booksRepository;
    }


    public Books addBooks(Books books) {
        validateBooksTitleExists(books);
        return booksRepository.save(books);
    }

    public void deleteBooks(Long bookNumber) {
        Books books = booksRepository.findById(bookNumber)
                .orElseThrow(()-> new BooksException(BooksError.BOOKS_NOT_FOUND));
        booksRepository.delete(books);

    }
// Pobiera listę książek po zadeklarowanym statusie
    public List<Books> getBooks(Books.Status status) {
        if (status != null) {
            return booksRepository.findAllByStatus(status);
        }
        return booksRepository.findAll();
    }

    public List<Books> getBooksByAutor(String autor){
        if (autor !=null && !autor.isEmpty()){
            return booksRepository.findAllByAutor(autor);
        }
        return booksRepository.findAll();
    }

    public List<Books> getBooksByCategory(final String category) {
        if (category !=null){
            return booksRepository.findAllByCategory(category);
        }
        return booksRepository.findAll();
    }


    public Books getBooks(Long booksNumber) {
        final Books books = booksRepository.findById(booksNumber)
                .orElseThrow(()-> new BooksException(BooksError.BOOKS_NOT_FOUND));
        return books;

    }

    public Books patchBooks(final Long bookNumber, Books books) {
        return booksRepository.findById(bookNumber)
                .map(booksFromDb ->{
                    if (!StringUtils.isEmpty(books.getTitle())){
                        booksFromDb.setTitle(books.getTitle());
                    }
                    if (!StringUtils.isEmpty(books.getCategory())){
                        booksFromDb.setCategory(books.getCategory());
                    }
                    if (!StringUtils.isEmpty(books.getStatus())){
                        booksFromDb. setStatus(books.getStatus());
                    }
                    if (!StringUtils.isEmpty(books.getAutor()))
                        booksFromDb.setAutor(books.getAutor());
                    return booksRepository.save(booksFromDb);
                }).orElseThrow(()-> new BooksException(BooksError.BOOKS_NOT_FOUND));
    }

    public Books putBooks( Long bookNumber,  Books books) {
        return booksRepository.findById(bookNumber)
                .map(booksFromDb->{
                    if (booksRepository.existsByTitle(books.getTitle()) &&
                    !booksFromDb.getTitle().equals(books.getTitle())
                    ) {
                        throw new BooksException(BooksError.BOOKS_ALREADY_EXISTS);
                    }
                    booksFromDb.setTitle(books.getTitle());
                    booksFromDb.setCategory(books.getCategory());
                    booksFromDb.setStatus(books.getStatus());
                    return booksRepository.save(booksFromDb);

                }).orElseThrow(()->new BooksException(BooksError.BOOKS_NOT_FOUND));
    }


    public Books updateBooksStatus(Long bookNumber, Books.Status newStatus) {
        Books bookToUpdate = booksRepository.findById(bookNumber)
                .orElseThrow(()-> new BooksException(BooksError.BOOKS_NOT_FOUND));
        bookToUpdate.setStatus(newStatus);
        booksRepository.save(bookToUpdate);

        return bookToUpdate;
    }

     // ta metoda sprawdza czy ksiazka o tym samym tytule istenieje.
    private void validateBooksTitleExists(Books books) {
        if (booksRepository.existsByTitle(books.getTitle())){
            throw new BooksException(BooksError.BOOKS_ALREADY_EXISTS);
        }
    }


}
