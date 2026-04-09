import { TimeRange } from "@shared/model/time-range";

export class SlotResponseDto {
    eventId!:number;
    id!: number;
    slotName!: string;
    slotDescription?:string;
    slotStartTime!: string;
    slotEndTime!: string;
    maxBookingsPerIdentity!: number | null;
    maxBookPerInterval!: number;
    slotIntervalMinutes!: number;
    slotFrequencyIntervalMinutes?: number;
    createdAt!: string;
    updatedAt!: string;
    businessDaysHours?: Record<number,TimeRange[]>;
    businessTimeZone?: string;
    businessAllowOt?: boolean;
    flexibleDaysHours?: TimeRange[];
    bookingsCount!: number;
}
