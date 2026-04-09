import { SlotResponseDto } from "@features/slots/dtos/slot-response-dto";

export class BookingResponseDto {

    id!: number;
    username?: string;
    lastName?: string;
    firstName?: string;
    guestLastName? : string;
    guestFirstName?: string;
    isGuest! : boolean;
    email!: string;
    slot!: SlotResponseDto;
    bookedStartTime!: string;
    bookedEndTime!: string;
    bookingToken!: string;
    bookedAt!: string;
    bookingStatus!: string;
}
