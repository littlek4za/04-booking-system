export class InvitationValidationResponseDto {
    valid!:boolean;
    requiredLogin!:boolean;
    token!:string;
    eventTitle!: string;
    expiredsAt!: string;
    reason!: string;
}
