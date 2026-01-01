export class EventRequestDto {
    eventName!:String;
    eventDescription!:String;
    eventLocationName!:String;
    includePosition!:boolean;
    latitude?:number;
    longitutde?:number;
    slotType!: 'FIXED'|'FLEXIBLE'|'BUSINESS'|'TESTFAIL';

}
