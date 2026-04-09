import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BookingRequestDto } from './dtos/booking-request-dto';
import { BehaviorSubject, catchError, filter, Observable, of, switchMap, tap } from 'rxjs';
import { BookingResponseDto } from './dtos/booking-response-dto';

@Injectable({
  providedIn: 'root',
})
export class BookingService {

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

  softDeleteForBooking(slotId:number, bookingId: number): Observable<BookingResponseDto>{
    const bookingsUrl = `${this.slotsUrl}/${slotId}/bookings/${bookingId}/delete`;
    return this.httpClient.patch<BookingResponseDto>(bookingsUrl,{});
  }
}
