export class SlotRequestDto {
    eventId!:number;
    slotName!: string;
    slotDescription?:string;
    slotStartTime!: string;
    slotEndTime!: string;
    maxBook!: number;
    slotIntervalMinutes!: number;
}
