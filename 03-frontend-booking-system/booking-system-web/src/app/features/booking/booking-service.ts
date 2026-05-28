import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BookingRequestDto } from './dtos/booking-request-dto';
import { BehaviorSubject, catchError, filter, Observable, of, switchMap, tap, throwError } from 'rxjs';
import { OrganizerBookingResponseDto } from './dtos/organizer-booking-response-dto';
import { GuestBookingViewInitRequestDto } from './dtos/guest-booking-view-init-request-dto';
import { GuestBookingViewInitResponseDto } from './dtos/guest-booking-view-init-response-dto';
import { GuestBookingViewAccessRequestDto } from './dtos/guest-booking-view-access-request-dto';
import { GuestAccessTokenDto } from '@features/auth/dtos/guest-access-token-dto';
import { AttendeeBookingResponseDto } from './dtos/attendee-booking-response-dto';
import { GuestBookingCreateInitRequestDto } from './dtos/guest-booking-create-init-request-dto';
import { GuestBookingCreateInitResponseDto } from './dtos/guest-booking-create-init-response-dto';
import { GuestBookingCreateAccessRequestDto } from './dtos/guest-booking-create-access-request-dto';
import { SlotBookedTimeResponseDto } from './dtos/slot-booked-time-response-dto';
import { environment } from '../../../environments/environment';
import { LoggerService } from '@core/services/logger-service';

@Injectable({
  providedIn: 'root',
})
export class BookingService {

  private backendApiUrl = `${environment.backendApiUrl}`;

  private logger = inject(LoggerService);

  private organizerBookingListSlotId$ = new BehaviorSubject<number | null>(null);
  organizerBookingListBySlotId$ = this.organizerBookingListSlotId$.pipe(
    filter((id): id is number => id !== null),
    switchMap(id => this.getOrganizerBookingsBySlotId(id).pipe(
      catchError(() => {
        return of([]);
      })
    ))
  )
  private slotBookedTimesSlotId$ = new BehaviorSubject<number | null>(null);
  slotBookedTimesBySlotId$ = this.slotBookedTimesSlotId$.pipe(
    filter((id): id is number => id !== null),
    switchMap(id => this.getSlotBookedTimesBySlotId(id).pipe(
      catchError(() => {
        return of([]);
      })
    ))
  )

  private eventId$ = new BehaviorSubject<number | null>(null);
  organizerBookingListByEventId$ = this.eventId$.pipe(
    filter((id): id is number => id !== null),
    switchMap(id => this.getOrganizerBookingsByEventId(id).pipe(
      catchError(() => {
        return of([]);
      })
    ))
  )

  constructor(private httpClient: HttpClient) { }

  createBooking(bookingRequestDto: BookingRequestDto, slotId: number): Observable<AttendeeBookingResponseDto> {
    const url = `${this.backendApiUrl}/v1/slots/${slotId}/bookings`;
    return this.httpClient.post<AttendeeBookingResponseDto>(`${url}`, bookingRequestDto).pipe(
      tap(() => {
        this.logger.info('[BookingService] Create booking successful');
      }),
      catchError(error => {
        this.logger.warn('[BookingService] Create booking failed');
        return throwError(() => error);
      })
    );
  }

  getOrganizerBookingsBySlotId(slotId: number): Observable<OrganizerBookingResponseDto[]> {
    const url = `${this.backendApiUrl}/v1/slots/${slotId}/bookings`;
    return this.httpClient.get<OrganizerBookingResponseDto[]>(url).pipe(
      tap(() => {
        this.logger.debug('[BookingService] Get organizer booking response dto successful');
      }),
      catchError(error => {
        this.logger.warn('[BookingService] Get organizer booking response dto successful failed');
        return throwError(() => error);
      })
    );
  }

  getSlotBookedTimesBySlotId(slotId: number): Observable<SlotBookedTimeResponseDto[]> {
    const url = `${this.backendApiUrl}/v1/slots/${slotId}/booked-times`;
    return this.httpClient.get<SlotBookedTimeResponseDto[]>(url).pipe(
      tap(() => {
        this.logger.debug('[BookingService] Get slot booked times successful');
      }),
      catchError(error => {
        this.logger.warn('[BookingService] Get slot booked times failed');
        return throwError(() => error);
      })
    );
  }

  getOrganizerBookingsByEventId(eventId: number): Observable<OrganizerBookingResponseDto[]> {
    const url = `${environment.backendApiUrl}/v1/events/${eventId}/bookings`;
    return this.httpClient.get<OrganizerBookingResponseDto[]>(url).pipe(
      tap(() => {
        this.logger.debug('[BookingService] Get organizer bookings successful');
      }),
      catchError(error => {
        this.logger.warn('[BookingService] Get organizer bookings failed');
        return throwError(() => error);
      })
    );
  }

  triggerRefreshForOrganizerBookingListBySlotId(slotId: number) {
    this.organizerBookingListSlotId$.next(slotId);
  }

  triggerRefreshForSlotBookedTimesBySlotId(slotId: number) {
    this.slotBookedTimesSlotId$.next(slotId);
  }

  triggerRefreshForOrganizerBookingListByEventId(eventId: number) {
    this.eventId$.next(eventId);
  }

  softDeleteBookingAsOrganizer(slotId: number, bookingId: number): Observable<OrganizerBookingResponseDto> {
    const url = `${this.backendApiUrl}/v1/slots/${slotId}/bookings/${bookingId}/delete`;
    return this.httpClient.patch<OrganizerBookingResponseDto>(url, {}).pipe(
      tap(() => {
        this.logger.info('[BookingService] Soft delete booking as organizer successful',
          {
            slotId: slotId,
            bookingId: bookingId,
          }
        );
      }),
      catchError(error => {
        this.logger.warn('[BookingService] Soft delete booking as attendee failed');
        return throwError(() => error);
      })
    );
  }

  softDeleteBookingAsAttendee(bookingId: number): Observable<number> {
    const url = `${this.backendApiUrl}/v1/bookings/${bookingId}/delete`;
    return this.httpClient.patch<number>(url, {}).pipe(
      tap(() => {
        this.logger.info('[BookingService] Soft delete booking as attendee successful', {bookingId: bookingId});
      }),
      catchError(error => {
        this.logger.warn('[BookingService] Soft delete booking as attendee failed');
        return throwError(() => error);
      })
    );
  }

  getBookingByBookingToken(bookingToken: string): Observable<AttendeeBookingResponseDto> {
    const url = `${this.backendApiUrl}/v1/bookings/${bookingToken}`;
    return this.httpClient.get<AttendeeBookingResponseDto>(url).pipe(
      tap(() => {
        this.logger.debug('[BookingService] Get booking by booking token successful');
      }),
      catchError(error => {
        this.logger.warn('[BookingService] Get booking by booking token failed');
        return throwError(() => error);
      })
    );
  }

  initGuestBookingViewAccess(dto: GuestBookingViewInitRequestDto): Observable<GuestBookingViewInitResponseDto> {
    const url = `${this.backendApiUrl}/v1/guest/bookings/view/init`;
    return this.httpClient.post<GuestBookingViewInitResponseDto>(url, dto).pipe(
      tap(() => {
        this.logger.debug('[BookingService] Get guest booking view init response successful');
      }),
      catchError(error => {
        this.logger.warn('[BookingService] Get guest booking view init response failed');
        return throwError(() => error);
      })
    );
  }

  issueGuestBookingViewAccessToken(dto: GuestBookingViewAccessRequestDto): Observable<GuestAccessTokenDto> {
    const url = `${this.backendApiUrl}/v1/guest/bookings/view/access`;
    return this.httpClient.post<GuestAccessTokenDto>(url, dto).pipe(
      tap(() => {
        this.logger.info('[BookingService] Guest token issued successfully');
      }),
      catchError(error => {
        this.logger.warn('[BookingService] Guest token issued failed');
        return throwError(() => error);
      })
    );
  }

  getAttendeeBookings(): Observable<AttendeeBookingResponseDto[]> {
    const url = `${this.backendApiUrl}/v1/bookings`;
    return this.httpClient.get<AttendeeBookingResponseDto[]>(url).pipe(
      tap(() => {
        this.logger.debug('[BookingService] Get attendee bookings successful');
      }),
      catchError(error => {
        this.logger.warn('[BookingService] Get attendee bookings failed');
        return throwError(() => error);
      })
    );
  }

  initGuestBookingCreateAccess(dto: GuestBookingCreateInitRequestDto): Observable<GuestBookingCreateInitResponseDto> {
    const url = `${this.backendApiUrl}/v1/guest/bookings/create/init`;
    return this.httpClient.post<GuestBookingCreateInitResponseDto>(url, dto).pipe(
      tap(() => {
        this.logger.debug('[BookingService] Get guest booking create init response successful');
      }),
      catchError(error => {
        this.logger.warn('[BookingService] Get guest booking create init response failed');
        return throwError(() => error);
      })
    );
  }

  issueGuestBookingCreateAccessToken(dto: GuestBookingCreateAccessRequestDto): Observable<GuestAccessTokenDto> {
    const url = `${this.backendApiUrl}/v1/guest/bookings/create/access`;
    return this.httpClient.post<GuestAccessTokenDto>(url, dto).pipe(
      tap(() => {
        this.logger.info('[BookingService] Guest token issued successfully');
      }),
      catchError(error => {
        this.logger.warn('[BookingService] Guest token issued failed');
        return throwError(() => error);
      })
    );
  }

}
