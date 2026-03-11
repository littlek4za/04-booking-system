import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BookingRequestDto } from './dtos/booking-request-dto';
import { Observable } from 'rxjs';
import { BookingResponseDto } from './dtos/booking-response-dto';

@Injectable({
  providedIn: 'root',
})
export class BookingService {
  
  private bookingsUrl = "http://localhost:8080/api/v1/bookings"
  
  constructor(private httpClient:HttpClient) {}

  createBooking(bookingRequestDto: BookingRequestDto): Observable<BookingResponseDto> {
    return this.httpClient.post<BookingResponseDto>(`${this.bookingsUrl}`, bookingRequestDto);
  }

}
