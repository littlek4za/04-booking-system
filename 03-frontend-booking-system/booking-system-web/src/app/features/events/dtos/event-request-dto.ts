export class EventRequestDto {
    eventName!:String;
    eventDescription!:String;
    eventLocationAddress!:String;
    includePosition!:boolean;
    latitude?:number;
    longitude?:number;
    slotType!: 'FIXED'|'FLEXIBLE'|'BUSINESS'|'TESTFAIL';

}
