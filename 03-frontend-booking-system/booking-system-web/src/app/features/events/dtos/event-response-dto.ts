import { EventTypeModel } from "./event-type-model";

export class EventResponseDto {
    id!:number;
    username?:string;
    eventName!:string;
    eventDescription?:string;
    eventLocationAddress!:string;
    includePosition!:boolean;
    latitude?:number;
    longitude?:number;
    eventType!: EventTypeModel;
    createdAt!: string;
    updatedAt!: string;
}