import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BookingRequestDto } from './dtos/booking-request-dto';
import { BehaviorSubject, catchError, filter, Observable, of, switchMap, tap } from 'rxjs';
import { BookingResponseDto } from './dtos/booking-response-dto';

@Injectable({
  providedIn: 'root',
})
export class BookingService {

  private slotsUrl = "http://localhost:8080/api/v1/slots"

  private slotId$ = new BehaviorSubject<number | null>(null);
  bookingList$ = this.slotId$.pipe(
    filter((id): id is number => id !== null),
    switchMap(id => this.getBookingsBySlot(id).pipe(
      tap((res) => console.log('GET Booking List succeed', res)),
      catchError(err => {
        console.log('GET Booking list by slot failed');
        return of([]);
      })
    ))
  )

  constructor(private httpClient: HttpClient) { }

  createBooking(bookingRequestDto: BookingRequestDto, slotId: number): Observable<BookingResponseDto> {
    const bookingsUrl = `${this.slotsUrl}/${slotId}/bookings`;
    return this.httpClient.post<BookingResponseDto>(`${bookingsUrl}`, bookingRequestDto);
  }

  getBookingsBySlot(slotId: number): Observable<BookingResponseDto[]> {
    const bookingsUrl = `${this.slotsUrl}/${slotId}/bookings`;
    return this.httpClient.get<BookingResponseDto[]>(bookingsUrl);
  }

  triggerRefreshForBookingList(slotId: number) {
    this.slotId$.next(slotId);
  }
}
