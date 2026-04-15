package com.littlek4za.booking_system.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.dtos.BookingResponseDto;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendBookingConfirmationDetails(BookingResponseDto dto){

        String link = frontendUrl + "/my-booking?token=" + dto.bookingToken();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(dto.email());
        message.setSubject("Your Booking Confirmation");
        message.setText(
            "Your booking is confirmed.\n\n"+
            "View or cancel your booking: \n" + link
        );

        mailSender.send(message);
    }
}
