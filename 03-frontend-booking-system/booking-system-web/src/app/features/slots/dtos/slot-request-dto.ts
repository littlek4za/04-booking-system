import { TimeRange } from "@shared/model/time-range";

export class SlotRequestDto {
    eventId!:number;
    slotName!: string;
    slotDescription?:string;
    slotStartTime!: string; //ISO string
    slotEndTime!: string; //ISO string
    maxBook?: number;
    slotIntervalMinutes?: number;
    slotFrequencyIntervalMinutes?: number;
    businessDaysHours?: Record<number,TimeRange[]>;
    flexibleDaysHours?: TimeRange[];
}
