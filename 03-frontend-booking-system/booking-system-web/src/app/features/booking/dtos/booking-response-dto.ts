import { SlotResponseDto } from "@features/slots/dtos/slot-response-dto";

export class BookingResponseDto {

    id!: number;
    username?: string;
    lastName!: string;
    firstName!: string;
    email!: string;
    slot!: SlotResponseDto;
    bookedStartTime!: string;
    bookedEndTime!: string;
    bookingToken!: string;
}
