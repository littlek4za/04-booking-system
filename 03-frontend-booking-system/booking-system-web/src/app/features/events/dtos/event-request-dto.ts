export class EventRequestDto {
    eventName!:String;
    eventDescription!:String;
    eventLocationAddress!:String;
    includePosition!:boolean;
    latitude?:number;
    longitude?:number;
    eventType!: 'FIXED'|'FLEXIBLE'|'BUSINESS'|'TESTFAIL';

}
