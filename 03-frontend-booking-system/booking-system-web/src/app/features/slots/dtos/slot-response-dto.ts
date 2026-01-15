export class SlotResponseDto {
    eventId!:number;
    id!: number;
    slotName!: string;
    slotDescription?:string;
    slotStartTime!: string;
    slotEndTime!: string;
    maxBook!: number;
    slotIntervalMinutes!: number;
    createdAt!: string;
    updatedAt!: string;
}
