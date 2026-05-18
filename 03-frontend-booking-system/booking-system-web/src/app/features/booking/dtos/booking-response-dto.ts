import { EventResponseDto } from "@features/events/dtos/event-response-dto";
import { SlotResponseDto } from "@features/slots/dtos/slot-response-dto";

export class OrganizerBookingResponseDto {

    bookingId!: number;
    attendeeUsername?: string;
    attendeeLastName?: string;
    attendeeFirstName?: string;
    guestAttendeeLastName? : string;
    guestAttendeeFirstName?: string;
    isGuest! : boolean;
    attendeeEmail!: string;
    slot!: SlotResponseDto;
    bookedStartTime!: string;
    bookedEndTime!: string;
    bookingToken!: string;
    bookedAt!: string;
    bookingStatus!: string;
    event!: EventResponseDto;
}
