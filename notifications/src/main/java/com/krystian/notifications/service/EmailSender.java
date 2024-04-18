package com.krystian.notifications.service;

import com.krystian.notifications.Dto.NotificationInfoDto;
import jakarta.mail.MessagingException;

public interface EmailSender {


    //   void sendEmails(NotificationInfoDto notoficationInfo);

    //void sendEmail(EmailDto emailDto) throws MessagingException;
    //  void sendEmail(EmailDto emailDto) throws MessagingException;

    void sendCustomEmail(String to, String subject, String content) throws MessagingException;

    void sendAddCustomer(NotificationInfoDto notificationInfoDto) throws MessagingException;

    void sendEnrollmentEmail(NotificationInfoDto notificationInfoDto) throws MessagingException;

    void sendDeactivationEmail(NotificationInfoDto notificationInfoDto) throws MessagingException;

    void sendDeleteCustomer(NotificationInfoDto notificationInfoDto) throws MessagingException;

    void sendDeleteCustomersBook(NotificationInfoDto notificationInfoDto) throws MessagingException;

    void sendReminderEmail (NotificationInfoDto notificationInfoDto) throws MessagingException;


    void sendReservationBooks(NotificationInfoDto notificationInfoDto) throws MessagingException;

    void sendUnReservationBooks(NotificationInfoDto notificationInfoDto) throws  MessagingException;

    void sendExtendReturnDate(NotificationInfoDto notificationInfoDto) throws MessagingException;

    void sendBlockCustomer(NotificationInfoDto notificationInfoDto) throws MessagingException;

    void sendUnBlockCustomer(NotificationInfoDto notificationInfoDto) throws MessagingException;
}
