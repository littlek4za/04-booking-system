package com.littlek4za.booking_system.services;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.littlek4za.booking_system.dtos.OrganizerBookingResponseDto;
import com.littlek4za.booking_system.entities.Booking;
import com.littlek4za.booking_system.repos.BookingRepository;
import com.littlek4za.booking_system.services.event.BookingMailEvent;
import com.resend.*;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

        @Value("${app.frontend.url}")
        private String frontendUrl;

        private final JavaMailSender mailSender;
        private final Resend resend;

        private final BookingRepository bookingRepository;

        public EmailService(Resend resend, BookingRepository bookingRepository, JavaMailSender mailSender) {
                this.mailSender = mailSender;
                this.resend = resend;
                this.bookingRepository = bookingRepository;
        }

        @Async
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void handleBookingEvent(BookingMailEvent event) {
                try {
                        if (event.type() == BookingMailEvent.MailType.CONFIRMATION) {
                                sendBookingConfirmationDetailsViaNormalMail(event.dto());
                        }

                        if (event.type() == BookingMailEvent.MailType.CANCELLATION) {
                                Booking booking = bookingRepository
                                                .findByIdAndIsDeletedTrueWithUser(event.bookingId())
                                                .orElse(null);

                                if (booking == null) {
                                        log.error(
                                                        "Failed to send cancellation email. Deleted booking not found. bookingId={}",
                                                        event.bookingId());
                                        return;
                                }

                                sendBookingCancelledDetailsViaNormalMail(booking);
                        }
                } catch (Exception e) {
                        log.error("Failed to process booking mail event for type={}",event.type(), e);
                }

        }

        public void sendBookingConfirmationDetailsViaNormalMail(OrganizerBookingResponseDto dto) {

                String link = frontendUrl + "/my-booking/" + dto.bookingToken();

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(dto.attendeeEmail());
                message.setSubject("Your Booking Confirmation");
                message.setText(
                                "Your booking is confirmed.\n\n" +
                                                "View or cancel your booking: \n" + link);

                mailSender.send(message);

        }

        public void sendBookingConfirmationDetailsViaResend(OrganizerBookingResponseDto dto) {

                String link = frontendUrl + "/my-booking/" + dto.bookingToken();

                CreateEmailOptions params = CreateEmailOptions.builder()
                                .from("onboarding@resend.dev")
                                .to("littlek4za@hotmail.com")
                                .subject("Your Booking Confirmation")
                                .html(
                                                "<h3>Your booking is confirmed.</h3>" +
                                                                "<br/>" +
                                                                "<p>View or cancel your booking:</p>" +
                                                                "<p><a href='" + link
                                                                + "'>Click here to manage your booking</a></p>")
                                .build();

                try {
                        CreateEmailResponse data = resend.emails().send(params);
                        log.info("RESEND Email sent, id={}", data.getId());
                } catch (Exception e) {
                        log.warn("RESEND Email failed", e);
                }
        }

        public void sendBookingCancelledDetailsViaNormalMail(Booking booking) {
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

        public void sendBookingCancelledDetailsViaResend(Booking booking) {

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

                String html = String.format(
                                "<div style='font-family:Arial,sans-serif; line-height:1.6; color:#333;'>" +

                                                "<p>Dear %s %s,</p>" +

                                                "<p>Your booking has been <strong style='color:#d9534f;'>cancelled</strong> by the organizer.</p>"
                                                +

                                                "<h3 style='margin-top:20px;'>Booking Details</h3>" +
                                                "<hr/>" +

                                                "<p><strong>Event:</strong> %s</p>" +
                                                "<p><strong>Slot:</strong> %s</p>" +
                                                "<p><strong>Time:</strong> %s - %s</p>" +
                                                "<p><strong>Location:</strong> %s</p>" +
                                                "<p><strong>Reference:</strong> %s</p>" +

                                                "<h3 style='margin-top:20px;'>Organizer Contact</h3>" +
                                                "<hr/>" +

                                                "<p><strong>Email:</strong> %s</p>" +

                                                "<p style='margin-top:20px;'>" +
                                                "If you have any questions regarding this cancellation, please contact the organizer directly."
                                                +
                                                "</p>" +

                                                "<p>Thank you.</p>" +

                                                "</div>",
                                firstName,
                                lastName,
                                booking.getEventName(),
                                booking.getSlotName(),
                                formattedZonedStart,
                                formattedZonedEnd,
                                booking.getEventLocationAddress(),
                                booking.getBookingToken(),
                                booking.getOrganizerEmail());

                CreateEmailOptions params = CreateEmailOptions.builder()
                                .from("onboarding@resend.dev")
                                .to("littlek4za@hotmail.com")
                                .subject("Booking Cancellation")
                                .html(html)
                                .build();

                try {
                        CreateEmailResponse data = resend.emails().send(params);
                        log.info("RESEND Email sent, id={}", data.getId());
                } catch (Exception e) {
                        log.warn("RESEND Email failed",e);
                }
        }
}
