package com.littlek4za.booking_system.services;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.dtos.OrganizerBookingResponseDto;
import com.littlek4za.booking_system.entities.Booking;

@Service
public class EmailService {

        @Autowired
        private JavaMailSender mailSender;

        @Value("${app.frontend.url}")
        private String frontendUrl;

        public void sendBookingConfirmationDetails(OrganizerBookingResponseDto dto) {

                String link = frontendUrl + "/my-booking/" + dto.bookingToken();

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(dto.attendeeEmail());
                message.setSubject("Your Booking Confirmation");
                message.setText(
                                "Your booking is confirmed.\n\n" +
                                                "View or cancel your booking: \n" + link);

                mailSender.send(message);
        }

        public void sendBookingCancelledDetails(Booking booking) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(booking.getUser().getEmail());
                message.setSubject("Booking Cancellation");

                boolean isGuest = booking.getUser().getGuest();
                String firstName;
                String lastName;

                if (isGuest) {
                        firstName = booking.getGuestFirstName();
                        lastName = booking.getGuestLastName();

                } else {
                        firstName = booking.getUser().getFirstName();
                        lastName = booking.getUser().getLastName();
                }

                ZonedDateTime zonedStart = booking.getBookedStartTime()
                                .atZone(ZoneOffset.UTC)
                                .withZoneSameInstant(ZoneId.of("Asia/Kuala_Lumpur"));

                String formattedZonedStart = zonedStart.format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a (XXX)"));

                ZonedDateTime zonedEnd = booking.getBookedEndTime()
                                .atZone(ZoneOffset.UTC)
                                .withZoneSameInstant(ZoneId.of("Asia/Kuala_Lumpur"));

                String formattedZonedEnd = zonedEnd.format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a (XXX)"));

                String text = String.format(
                                """
                                                Dear %s %s,

                                                Your booking has been cancelled by the organizer.

                                                Booking Details:
                                                --------------------------------
                                                Event      : %s
                                                Slot       : %s
                                                Time       : %s - %s
                                                Location   : %s
                                                Reference  : %s

                                                Organizer Contact
                                                --------------------------------
                                                Email      : %s

                                                If you have any questions regarding this cancellation, please contact the organizer directly.

                                                Thank you.
                                                """,
                                firstName,
                                lastName,
                                booking.getEventName(),
                                booking.getSlotName(),
                                formattedZonedStart,
                                formattedZonedEnd,
                                booking.getEventLocationAddress(),
                                booking.getBookingToken(),
                                booking.getOrganizerEmail());

                message.setText(text);

                mailSender.send(message);

        }
}
