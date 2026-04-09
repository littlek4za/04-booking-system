import { EventTypeModel } from "./event-type-model";

export class EventWithSlotCountResponseDto {
    id!: number;
    eventName!: string;
    eventDescription?: string;
    eventLocationAddress!: string;
    includePosition!: boolean;
    latitude?: number;
    longitude?: number;
    maxBookingsPerIdentity!: number | null;
    eventType!: EventTypeModel;
    createdAt!: string;
    updatedAt!: string;
    slotCount!: number;

}
