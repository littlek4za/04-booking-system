import { EventTypeModel } from "./event-type-model";

export class EventRequestDto {
    eventName!:String;
    eventDescription!:String;
    eventLocationAddress!:String;
    includePosition!:boolean;
    latitude?:number;
    longitude?:number;
    eventType!: EventTypeModel;

}
