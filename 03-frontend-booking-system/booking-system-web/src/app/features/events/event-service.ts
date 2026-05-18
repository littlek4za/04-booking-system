import { inject, Injectable } from '@angular/core';
import { EventRequestDto } from './dtos/event-request-dto';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, Observable, of, startWith, Subject, switchMap, tap, throwError } from 'rxjs';
import { EventResponseDto } from './dtos/event-response-dto';
import { EventWithSlotCountResponseDto } from './dtos/event-with-slot-count-response-dto';
import { DeleteValidationResponseDto } from './dtos/delete-validation-response-dto';
import { LoggerService } from '@core/services/logger-service';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class EventService {

  private logger = inject(LoggerService);
  private eventsUrl = `${environment.backendApiUrl}/v1/events`;

  private refreshTrigger$ = new Subject<void> (); 
  eventList$ = this.refreshTrigger$.pipe(
    startWith(undefined), 
    switchMap(()=>this.getEvents().pipe(
      catchError(() => {
        return of([]);
      })
    ))
  );

  constructor(private httpClient: HttpClient, private router: Router) { }

  createEvent(eventRequestDto: EventRequestDto): Observable<EventResponseDto> {
    return this.httpClient.post<EventResponseDto>(this.eventsUrl, eventRequestDto).pipe(
      tap(() => {
        this.logger.info('[EventService] Create event successful');
      }),
      catchError(error => {
        this.logger.warn('[EventService] Create event failed');
        return throwError(() => error);
      })
    );
  }

  getEvents(): Observable<EventWithSlotCountResponseDto[]> {
    return this.httpClient.get<EventWithSlotCountResponseDto[]>(this.eventsUrl).pipe(
      tap(() => {
        this.logger.debug('[EventService] Get events successful');
      }),
      catchError(error => {
        this.logger.warn('[EventService] Get events failed');
        return throwError(() => error);
      })
    );
  }

  getEventById(eventId: number): Observable<EventWithSlotCountResponseDto> {
    return this.httpClient.get<EventWithSlotCountResponseDto>(`${this.eventsUrl}/${eventId}`).pipe(
      tap(() => {
        this.logger.debug('[EventService] Get event by id successful');
      }),
      catchError(error => {
        this.logger.warn('[EventService] Get event by id failed');
        return throwError(() => error);
      })
    );
  }

  putEventById(eventId: number, eventRequestDto: EventRequestDto): Observable<EventResponseDto> {
    return this.httpClient.put<EventResponseDto>(`${this.eventsUrl}/${eventId}`, eventRequestDto).pipe(
      tap(()=> this.logger.info(`[EventService] Update event successful`)),
      catchError(error => {
        this.logger.warn(`[EventService] Update event failed`);
        return throwError(() => error);
      })
    );
  }

  deleteEventById(eventId: number): Observable<void>{
    return this.httpClient.delete<void>(`${this.eventsUrl}/${eventId}`).pipe(
      tap(()=> this.logger.debug(`[EventService] Delete event by id successful`)),
      catchError(error => {
        this.logger.warn(`[EventService] Delete event by id failed`);
        return throwError(() => error);
      })
    );
  }

  eventDeleteValidation(eventId: number): Observable<DeleteValidationResponseDto>{
    return this.httpClient.get<DeleteValidationResponseDto>(`${this.eventsUrl}/${eventId}/delete-validation`).pipe(
      tap(()=> this.logger.debug(`[EventService] Get delete validation response successful`)),
      catchError(error => {
        this.logger.warn(`[EventService] Get delete validation response failed`);
        return throwError(() => error);
      })
    );
  }

  triggerRefresh() {
    this.refreshTrigger$.next();
  }

}
