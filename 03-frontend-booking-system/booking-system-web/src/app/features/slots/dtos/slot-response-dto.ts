import { TimeRange } from "@shared/model/time-range";

export class SlotResponseDto {
    eventId!:number;
    id!: number;
    slotName!: string;
    slotDescription?:string;
    slotStartTime!: string;
    slotEndTime!: string;
    maxBook!: number;
    slotIntervalMinutes!: number;
    slotFrequencyIntervalMinutes?: number;
    createdAt!: string;
    updatedAt!: string;
    workingDaysHours?: Record<number,TimeRange[]>;
}
