package com.littlek4za.booking_system.services.eventlistener;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.littlek4za.booking_system.dtos.AttendeeBookingResponseDto;
import com.littlek4za.booking_system.services.event.BookingMailEvent;
import com.resend.*;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailEventListener {

        @Value("${app.frontend.url}")
        private String frontendUrl;

        private final JavaMailSender mailSender;
        private final Resend resend;

        public EmailEventListener(Resend resend, JavaMailSender mailSender) {
                this.mailSender = mailSender;
                this.resend = resend;
        }

        @Async
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void handleBookingEvent(BookingMailEvent event) {
                try {
                        if (event.type() == BookingMailEvent.MailType.CONFIRMATION) {
                                sendBookingConfirmationDetailsViaNormalMail(event.dto());
                        }

                        if (event.type() == BookingMailEvent.MailType.ORGANIZER_CANCELLATION) {
                                sendOrganizerBookingCancelledDetailsViaNormalMail(event);
                        }

                        if (event.type() == BookingMailEvent.MailType.ATTENDEE_CANCELLATION) {
                                sendAttendeeBookingCancelledDetailsViaNormalMail(event);
                        }

                } catch (Exception e) {
                        log.error("Failed to process booking mail event for type={}", event.type(), e);
                }

        }


        public void sendBookingConfirmationDetailsViaNormalMail(AttendeeBookingResponseDto dto) {

                String link = frontendUrl + "/bookingAccess/" + dto.bookingToken();

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(dto.attendeeEmail());
                message.setSubject("Your Booking Confirmation");
                message.setText(
                                "Your booking is confirmed.\n\n" +
                                                "View or cancel your booking: \n" + link);

                mailSender.send(message);

        }

        public void sendBookingConfirmationDetailsViaResend(AttendeeBookingResponseDto dto) {

                String link = frontendUrl + "/bookingAccess/" + dto.bookingToken();

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

        public void sendOrganizerBookingCancelledDetailsViaNormalMail(BookingMailEvent bookingMailEvent) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(bookingMailEvent.attendeeEmail());
                message.setSubject("Booking Cancellation");

                ZonedDateTime zonedStart = bookingMailEvent.bookedStartTime()
                                .atZone(ZoneOffset.UTC)
                                .withZoneSameInstant(ZoneId.of("Asia/Kuala_Lumpur"));

                String formattedZonedStart = zonedStart.format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a (XXX)"));

                ZonedDateTime zonedEnd = bookingMailEvent.bookedEndTime()
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
                                bookingMailEvent.attendeeFirstName(),
                                bookingMailEvent.attendeeLastName(),
                                bookingMailEvent.eventName(),
                                bookingMailEvent.slotName(),
                                formattedZonedStart,
                                formattedZonedEnd,
                                bookingMailEvent.eventLocationAddress(),
                                bookingMailEvent.bookingToken(),
                                bookingMailEvent.organizerEmail());

                message.setText(text);

                mailSender.send(message);

        }

        public void sendOrganizerBookingCancelledDetailsViaResend(BookingMailEvent bookingMailEvent) {

                ZonedDateTime zonedStart = bookingMailEvent.bookedStartTime()
                                .atZone(ZoneOffset.UTC)
                                .withZoneSameInstant(ZoneId.of("Asia/Kuala_Lumpur"));

                String formattedZonedStart = zonedStart.format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a (XXX)"));

                ZonedDateTime zonedEnd = bookingMailEvent.bookedEndTime()
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
                                bookingMailEvent.attendeeFirstName(),
                                bookingMailEvent.attendeeLastName(),
                                bookingMailEvent.eventName(),
                                bookingMailEvent.slotName(),
                                formattedZonedStart,
                                formattedZonedEnd,
                                bookingMailEvent.eventLocationAddress(),
                                bookingMailEvent.bookingToken(),
                                bookingMailEvent.organizerEmail()
                        );

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
                        log.warn("RESEND Email failed", e);
                }
        }

        private void sendAttendeeBookingCancelledDetailsViaNormalMail(BookingMailEvent bookingMailEvent) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(bookingMailEvent.organizerEmail());
                message.setSubject("Booking Cancellation By Attendee");

                ZonedDateTime zonedStart = bookingMailEvent.bookedStartTime()
                                .atZone(ZoneOffset.UTC)
                                .withZoneSameInstant(ZoneId.of("Asia/Kuala_Lumpur"));

                String formattedZonedStart = zonedStart.format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a (XXX)"));

                ZonedDateTime zonedEnd = bookingMailEvent.bookedEndTime()
                                .atZone(ZoneOffset.UTC)
                                .withZoneSameInstant(ZoneId.of("Asia/Kuala_Lumpur"));

                String formattedZonedEnd = zonedEnd.format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a (XXX)"));

                String text = String.format(
                                """
                                                Dear %s %s,

                                                Booking has been cancelled by the attendee.

                                                Booking Details:
                                                --------------------------------
                                                Event      : %s
                                                Slot       : %s
                                                Time       : %s - %s
                                                Location   : %s
                                                Reference  : %s

                                                Attendee Contact
                                                --------------------------------
                                                Email      : %s
                                                FirstName  : %s
                                                LastName   : %s

                                                Thank you.
                                                """,
                                bookingMailEvent.organizerFirstName(),
                                bookingMailEvent.organizerLastName(),
                                bookingMailEvent.eventName(),
                                bookingMailEvent.slotName(),
                                formattedZonedStart,
                                formattedZonedEnd,
                                bookingMailEvent.eventLocationAddress(),
                                bookingMailEvent.bookingToken(),
                                bookingMailEvent.attendeeEmail(),
                                bookingMailEvent.attendeeFirstName(),
                                bookingMailEvent.attendeeLastName()
                        );

                message.setText(text);

                mailSender.send(message);
        }

        public void sendAttendeeBookingCancelledDetailsViaResend(BookingMailEvent bookingMailEvent) {

                ZonedDateTime zonedStart = bookingMailEvent.bookedStartTime()
                                .atZone(ZoneOffset.UTC)
                                .withZoneSameInstant(ZoneId.of("Asia/Kuala_Lumpur"));

                String formattedZonedStart = zonedStart.format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a (XXX)"));

                ZonedDateTime zonedEnd = bookingMailEvent.bookedEndTime()
                                .atZone(ZoneOffset.UTC)
                                .withZoneSameInstant(ZoneId.of("Asia/Kuala_Lumpur"));

                String formattedZonedEnd = zonedEnd.format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a (XXX)"));

                String html = String.format(
                                "<div style='font-family:Arial,sans-serif; line-height:1.6; color:#333;'>" +

                                                "<p>Dear %s %s,</p>" +

                                                "<p>Booking has been <strong style='color:#d9534f;'>cancelled</strong> by the attendee.</p>"
                                                +

                                                "<h3 style='margin-top:20px;'>Booking Details</h3>" +
                                                "<hr/>" +

                                                "<p><strong>Event:</strong> %s</p>" +
                                                "<p><strong>Slot:</strong> %s</p>" +
                                                "<p><strong>Time:</strong> %s - %s</p>" +
                                                "<p><strong>Location:</strong> %s</p>" +
                                                "<p><strong>Reference:</strong> %s</p>" +

                                                "<h3 style='margin-top:20px;'>Attendee Contact</h3>" +
                                                "<hr/>" +

                                                "<p><strong>Email:</strong> %s</p>" +
                                                "<p><strong>First Name:</strong> %s</p>" +
                                                "<p><strong>Last Name :</strong> %s</p>" +

                                                "<p>Thank you.</p>" +

                                                "</div>",
                                bookingMailEvent.organizerFirstName(),
                                bookingMailEvent.organizerLastName(),
                                bookingMailEvent.eventName(),
                                bookingMailEvent.slotName(),
                                formattedZonedStart,
                                formattedZonedEnd,
                                bookingMailEvent.eventLocationAddress(),
                                bookingMailEvent.bookingToken(),
                                bookingMailEvent.attendeeEmail(),
                                bookingMailEvent.attendeeFirstName(),
                                bookingMailEvent.attendeeLastName()
                        );

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
                        log.warn("RESEND Email failed", e);
                }
        }
}
