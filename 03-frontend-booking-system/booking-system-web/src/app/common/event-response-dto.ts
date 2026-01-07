export class EventResponseDto {
    id!:number;
    username!:string;
    eventName!:string;
    eventDescription?:string;
    eventLocationName!:string;
    includePosition!:boolean;
    latitude?:number;
    longitude?:number;
    slotType!: 'FIXED'|'FLEXIBLE'|'BUSINESS';
    createdAt!: string;
}
