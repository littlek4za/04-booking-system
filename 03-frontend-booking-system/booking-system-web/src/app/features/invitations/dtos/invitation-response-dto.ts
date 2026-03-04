import { SlotIncludeMode } from "./slot-include-mode";

export class InvitationResponseDto {

    id!:number;
    eventId!:number;
    userId!:number;
    expiresAt!:string;
    maxUsage?: number | null;
    usedCount!: number;
    accessToken!: string;
    slotIncludeMode!: SlotIncludeMode;
    requiredLogin!: boolean;
    maxUsagePerUser?: number | null;
    createdAt!: string;

}
