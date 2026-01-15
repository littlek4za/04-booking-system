export class EventResponseDto {
    id!:number;
    username?:string;
    eventName!:string;
    eventDescription?:string;
    eventLocationAddress!:string;
    includePosition!:boolean;
    latitude?:number;
    longitude?:number;
    eventType!: 'FIXED'|'FLEXIBLE'|'BUSINESS';
    createdAt!: string;
    updatedAt!: string;
}