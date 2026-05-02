export class AttendeeBookingResponseDto {
    id!: number;
    username?: string;
    lastName?: string;
    firstName?: string;
    guestLastName?: string;
    guestFirstName?: string;
    isGuest!: boolean;
    email!: string;
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
