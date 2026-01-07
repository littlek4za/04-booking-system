export class EventSaveRequestDto {
    eventName!:String;
    eventDescription!:String;
    eventLocationName!:String;
    includePosition!:boolean;
    latitude?:number;
    longitude?:number;
    slotType!: 'FIXED'|'FLEXIBLE'|'BUSINESS'|'TESTFAIL';

}
