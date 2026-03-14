import { TimeRange } from "@shared/model/time-range";

export class SlotRequestDto {
    eventId!: number;
    slotName!: string;
    slotDescription?: string;
    slotStartTime!: string; //ISO string
    slotEndTime!: string; //ISO string
    maxBookPerInterval?: number;
    slotIntervalMinutes?: number;
    slotFrequencyIntervalMinutes?: number;
    businessDaysHours?: Record<number, TimeRange[]>;
    businessTimeZone?: string;
    businessAllowOt?: boolean;
    flexibleDaysHours?: TimeRange[];
}
