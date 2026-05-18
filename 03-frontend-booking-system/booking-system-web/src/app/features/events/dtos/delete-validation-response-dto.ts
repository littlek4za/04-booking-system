export class DeleteValidationResponseDto {
    canDelete!: boolean;
    upcomingBookingCount!: number;
    ongoingBookingCount!: number;
    expiredBookingCount!: number;
}
