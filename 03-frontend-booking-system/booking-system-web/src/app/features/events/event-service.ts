import { Injectable } from '@angular/core';
import { EventRequestDto } from './dtos/event-request-dto';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, catchError, Observable, of, switchMap, tap } from 'rxjs';
import { EventResponseDto } from './dtos/event-response-dto';
import { EventWithSlotCountResponseDto } from './dtos/event-with-slot-count-response-dto';

@Injectable({
  providedIn: 'root',
})
export class EventService {

  private eventsUrl = "http://localhost:8080/api/v1/events";

  private refreshTrigger$ = new BehaviorSubject<void> (undefined) ;
  eventList$ = this.refreshTrigger$.pipe( // become observable
    switchMap(()=>this.getEvents().pipe(
      tap(res => console.log('GET Event List succeed', res)),
      catchError(err => {
        console.log('GET Eventlist failed');
        return of([]);
      })
    ))
  );

  constructor(private httpClient: HttpClient, private router: Router) { }

  createEvent(eventRequestDto: EventRequestDto): Observable<EventResponseDto> {
    return this.httpClient.post<EventResponseDto>(this.eventsUrl, eventRequestDto);
  }

  getEvents(): Observable<EventWithSlotCountResponseDto[]> {
    return this.httpClient.get<EventWithSlotCountResponseDto[]>(this.eventsUrl);
  }

  getEventById(eventId: number): Observable<EventWithSlotCountResponseDto> {
    return this.httpClient.get<EventWithSlotCountResponseDto>(`${this.eventsUrl}/${eventId}`);
  }

  putEventById(eventId: number, eventRequestDto: EventRequestDto): Observable<EventResponseDto> {
    return this.httpClient.put<EventResponseDto>(`${this.eventsUrl}/${eventId}`, eventRequestDto);
  }

  deleteEventById(eventId: number): Observable<void>{
    return this.httpClient.delete<void>(`${this.eventsUrl}/${eventId}`);
  }

  triggerRefresh() {
    this.refreshTrigger$.next();
  }

}
