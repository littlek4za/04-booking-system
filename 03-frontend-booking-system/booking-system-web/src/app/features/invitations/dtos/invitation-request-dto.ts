import { SlotIncludeMode } from "./slot-include-mode";

export class InvitationRequestDto {

    expiresAt?: string;
    maxUsage?: number;
    maxUsagePerUser?: number;
    requiredLogin!: boolean;
    slotIncludeMode!: SlotIncludeMode;
    slotIdList?: number[];
}
