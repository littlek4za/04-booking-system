import { SlotResponseDto } from "@features/slots/dtos/slot-response-dto";
import { SlotIncludeMode } from "./slot-include-mode";
import { EventResponseDto } from "@features/events/dtos/event-response-dto";

export class InvitationResponseDto {

    id!:number;
    event!: EventResponseDto;
    userId!:number;
    expiresAt!:string;
    maxUsage?: number | null;
    usedCount!: number;
    accessToken!: string;
    slotIncludeMode!: SlotIncludeMode;
    requiredLogin!: boolean;
    maxUsagePerUser?: number | null;
    createdAt!: string;
    slotList! : SlotResponseDto[];

}
