export class GuestBookingCreateAccessRequestDto {
    email!:string;
    captchaToken!:string | null;
    invitationId!:number;
    slotId!:number;
}
