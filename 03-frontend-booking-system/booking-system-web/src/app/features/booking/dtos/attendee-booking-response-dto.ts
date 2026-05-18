export class AttendeeBookingResponseDto {
    bookingId!: number;
    attendeeUsername?: string;
    attendeeLastName?: string;
    attendeeFirstName?: string;
    guestAttendeeLastName?: string;
    guestAttendeeFirstName?: string;
    isGuest!: boolean;
    bookedStartTime!: string;
    bookedEndTime!: string;
    bookingToken!: string;
    bookedAt!: string;
    bookingStatus!: string;
    eventName!: string;
    slotName!: string;
    organizerEmail!: string;
    attendeeEmail!: string;
    eventLocationAddress!: string;
    latitude?: number;
    longitude?: number;
}
