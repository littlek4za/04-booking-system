import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BookingRequestDto } from './dtos/booking-request-dto';
import { BehaviorSubject, catchError, filter, Observable, of, switchMap, tap } from 'rxjs';
import { BookingResponseDto } from './dtos/booking-response-dto';
import { GuestBookingViewInitRequestDto } from './dtos/guest-booking-view-init-request-dto';
import { GuestBookingViewInitResponseDto } from './dtos/guest-booking-view-init-response-dto';
import { GuestBookingViewAccessRequestDto } from './dtos/guest-booking-view-access-request-dto';
import { GuestAccessTokenDto } from '@features/auth/dtos/guest-access-token-dto';
import { AttendeeBookingResponseDto } from './dtos/attendee-booking-response-dto';
import { GuestBookingCreateInitRequestDto } from './dtos/guest-booking-create-init-request-dto';
import { GuestBookingCreateInitResponseDto } from './dtos/guest-booking-create-init-response-dto';
import { GuestBookingCreateAccessRequestDto } from './dtos/guest-booking-create-access-request-dto';

@Injectable({
  providedIn: 'root',
})
export class BookingService {

  private url = "http://localhost:8080/api/v1";
  private slotsUrl = "http://localhost:8080/api/v1/slots";
  private eventsUrl = "http://localhost:8080/api/v1/events";

  private slotId$ = new BehaviorSubject<number | null>(null);
  bookingListBySlotId$ = this.slotId$.pipe(
    filter((id): id is number => id !== null),
    switchMap(id => this.getBookingsBySlotId(id).pipe(
      tap((res) => console.log('GET Booking List succeed', res)),
      catchError(err => {
        console.log('GET Booking list by slot failed');
        return of([]);
      })
    ))
  )

  private eventId$ = new BehaviorSubject<number | null>(null);
  bookingListByEventId$ = this.eventId$.pipe(
    filter((id): id is number => id !== null),
    switchMap(id => this.getBookingsByEventId(id).pipe(
      tap((res) => console.log('GET Booking List succeed', res)),
      catchError(err => {
        console.log('GET Booking list by event failed');
        return of([]);
      })
    ))
  )

  constructor(private httpClient: HttpClient) { }

  createBooking(bookingRequestDto: BookingRequestDto, slotId: number): Observable<BookingResponseDto> {
    const bookingsUrl = `${this.slotsUrl}/${slotId}/bookings`;
    return this.httpClient.post<BookingResponseDto>(`${bookingsUrl}`, bookingRequestDto);
  }

  getBookingsBySlotId(slotId: number): Observable<BookingResponseDto[]> {
    const bookingsUrl = `${this.slotsUrl}/${slotId}/bookings`;
    return this.httpClient.get<BookingResponseDto[]>(bookingsUrl);
  }

  getBookingsByEventId(eventId: number): Observable<BookingResponseDto[]> {
    const bookingsUrl = `${this.eventsUrl}/${eventId}/bookings`;
    return this.httpClient.get<BookingResponseDto[]>(bookingsUrl);
  }

  triggerRefreshForBookingListBySlotId(slotId: number) {
    this.slotId$.next(slotId);
  }

  triggerRefreshForBookingListByEventId(eventId: number) {
    this.eventId$.next(eventId);
  }

  softDeleteBookingAsOrganizer(slotId: number,bookingId: number): Observable<BookingResponseDto>{
    const bookingsUrl = `${this.slotsUrl}/${slotId}/bookings/${bookingId}/delete`;
    return this.httpClient.patch<BookingResponseDto>(bookingsUrl,{});
  }

  softDeleteBookingAsUserAttendee(bookingId: number): Observable<number>{
    const bookingsUrl = `${this.url}/bookings/${bookingId}/delete`;
    return this.httpClient.patch<number>(bookingsUrl,{});
  }

  getBookingByBookingToken(bookingToken: string): Observable<AttendeeBookingResponseDto>{
    const bookingsUrl = `${this.url}/bookings/${bookingToken}`;
    return this.httpClient.get<AttendeeBookingResponseDto>(bookingsUrl);
  }

  initGuestBookingViewAccess(dto: GuestBookingViewInitRequestDto): Observable<GuestBookingViewInitResponseDto>{
    const initGuestBookingViewAccessUrl = `${this.url}/guest/bookings/view/init`;
    return this.httpClient.post<GuestBookingViewInitResponseDto>(initGuestBookingViewAccessUrl,dto);
  }

  issueGuestBookingViewAccessToken(dto:GuestBookingViewAccessRequestDto): Observable<GuestAccessTokenDto>{
    const guestBookingViewAccessTokenRequestUrl = `${this.url}/guest/bookings/view/access`;
    return this.httpClient.post<GuestAccessTokenDto>(guestBookingViewAccessTokenRequestUrl, dto);
  }

  retrieveGuestBooking(guestBookingRequestDto: GuestBookingViewInitRequestDto): Observable<BookingResponseDto>{
    const bookingsUrl = `${this.url}/bookings/guest/access`;
    return this.httpClient.post<BookingResponseDto>(bookingsUrl,guestBookingRequestDto);
  }

  getUserBookings():Observable<AttendeeBookingResponseDto[]>{
    const bookingsUrl = `${this.url}/bookings`;
    return this.httpClient.get<AttendeeBookingResponseDto[]>(bookingsUrl);
  }

  initGuestBookingCreateAccess(dto: GuestBookingCreateInitRequestDto):Observable<GuestBookingCreateInitResponseDto>{
    const initGuestBookingCreateAccessUrl = `${this.url}/guest/bookings/create/init`;
    return this.httpClient .post<GuestBookingCreateInitResponseDto>(initGuestBookingCreateAccessUrl,dto);
  }

  issueGuestBookingCreateAccessToken(dto: GuestBookingCreateAccessRequestDto): Observable<GuestAccessTokenDto>{
    const guestBookingCreateAccessTokenRequestUrl = `${this.url}/guest/bookings/create/access`;
    return this.httpClient.post<GuestAccessTokenDto>(guestBookingCreateAccessTokenRequestUrl, dto);
  }

}
