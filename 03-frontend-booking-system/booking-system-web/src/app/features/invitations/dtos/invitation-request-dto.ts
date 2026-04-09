import { SlotIncludeMode } from "./slot-include-mode";

export class InvitationRequestDto {

    expiresAt?: string;
    maxUsage?: number;
    maxUsagePerIdentity?: number;
    requiredLogin!: boolean;
    slotIncludeMode!: SlotIncludeMode;
    slotIdList?: number[];
}
