import { EventTypeModel } from "./event-type-model";

export class EventResponseDto {
    id!:number;
    eventName!:string;
    eventDescription?:string;
    eventLocationAddress!:string;
    includePosition!:boolean;
    latitude!:number | null;
    longitude!:number | null;
    maxBookingsPerIdentity!: number | null;
    eventType!: EventTypeModel;
    createdAt!: string;
    updatedAt!: string;
}