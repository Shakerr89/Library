package com.krystian.notifications.controller;

import com.krystian.notifications.Dto.EmailDto;
import com.krystian.notifications.service.EmailSender;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class EmailController {

    private final EmailSender emailSender;

    public EmailController(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    // to służy do wysyłania maila z Api. Aktualnie nie działa
    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(@RequestBody @Valid EmailDto emailDto) {
        try {
            emailSender.sendCustomEmail(emailDto.getTo(), emailDto.getTitle(), emailDto.getContent());
        } catch (MessagingException e) {
            log.error("Wiadomość do " + emailDto.getTo() + " się nie wysłała ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Wiadomosc do " + emailDto.getTo() + " się nie wysłała!");
        }
        return ResponseEntity.ok("Wysłano wiadomość do: " + emailDto.getTo());
    }

}
