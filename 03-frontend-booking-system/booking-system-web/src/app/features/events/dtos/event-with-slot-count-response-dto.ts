import { EventTypeModel } from "./event-type-model";

export class EventWithSlotCountResponseDto {
    id!: number;
    eventName!: string;
    eventDescription?: string;
    eventLocationAddress!: string;
    includePosition!: boolean;
    latitude?: number;
    longitude?: number;
    eventType!: EventTypeModel;
    createdAt!: string;
    updatedAt!: string;
    slotCount!: number;

}
