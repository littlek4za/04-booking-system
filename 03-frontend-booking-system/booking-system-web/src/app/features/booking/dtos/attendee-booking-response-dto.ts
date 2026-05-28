export class AttendeeBookingResponseDto {
    bookingId!: number;
    attendeeLastName!: string;
    attendeeFirstName!: string;
    isGuest!: boolean;
    bookedStartTime!: string;
    bookedEndTime!: string;
    bookingToken!: string;
    bookedAt!: string;
    bookingStatus!: string;
    eventName!: string;
    eventDescription?: string;
    slotName!: string;
    slotDescription?: string;
    organizerEmail!: string;
    attendeeEmail!: string;
    eventLocationAddress!: string;
    latitude?: number;
    longitude?: number;
}
