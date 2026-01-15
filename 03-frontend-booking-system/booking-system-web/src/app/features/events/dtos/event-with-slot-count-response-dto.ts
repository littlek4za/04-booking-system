export class EventWithSlotCountResponseDto {
    id!: number;
    eventName!: string;
    eventDescription?: string;
    eventLocationAddress!: string;
    includePosition!: boolean;
    latitude?: number;
    longitude?: number;
    eventType!: 'FIXED' | 'FLEXIBLE' | 'BUSINESS';
    createdAt!: string;
    updatedAt!: string;
    slotCount!: number;

}
