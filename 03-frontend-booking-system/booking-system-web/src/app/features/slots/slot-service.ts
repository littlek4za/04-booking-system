import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, catchError, filter, Observable, of, switchMap, tap, throwError } from 'rxjs';
import { SlotResponseDto } from './dtos/slot-response-dto';
import { SlotRequestDto } from './dtos/slot-request-dto';
import { DeleteValidationResponseDto } from '@features/events/dtos/delete-validation-response-dto';
import { LoggerService } from '@core/services/logger-service';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class SlotService {

  private logger = inject(LoggerService);

  private eventId$ = new BehaviorSubject<number | null>(null);
  slotListByEventId$ = this.eventId$.pipe(
    filter((id): id is number => id !== null), //skip null
    switchMap(id => this.getSlotsByEventId(id).pipe(
      catchError(() => {
        return of([]);
      })
    ))
  );

  private eventsUrl = `${environment.backendApiUrl}/v1/events`;

  constructor(private httpClient: HttpClient) { }

  getSlotsByEventId(eventId: number): Observable<SlotResponseDto[]> {
    return this.httpClient.get<SlotResponseDto[]>(`${this.eventsUrl}/${eventId}/slots`).pipe(
      tap(() =>
        this.logger.debug(`[SlotService] GET slots by eventId successful`)
      ),
      catchError(error => {
        this.logger.warn(`[SlotService] GET slots by eventId failed`);
        return throwError(() => error);
      })
    );
  }

  triggerRefreshForSlotListByEventId(eventId: number) {
    this.eventId$.next(eventId);
  }

  createSlotByEventId(slotRequestDto: SlotRequestDto, eventId: number): Observable<SlotResponseDto> {
    return this.httpClient.post<SlotResponseDto>(`${this.eventsUrl}/${eventId}/slots`, slotRequestDto).pipe(
      tap(() =>
        this.logger.info(`[SlotService] CREATE slot successful`)
      ),
      catchError(error => {
        this.logger.warn(`[SlotService] CREATE slot failed`);
        return throwError(() => error);
      })
    );
  }

  deleteSlotByIdAndEvent(eventId: number, slotId: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.eventsUrl}/${eventId}/slots/${slotId}`).pipe(
      tap(() =>
        this.logger.info(`[SlotService] DELETE slot by slotId and eventId successful`)
      ),
      catchError(error => {
        this.logger.warn(`[SlotService] DELETE slot by slotId and eventId failed`);
        return throwError(() => error);
      })
    );
  }

  getSlotByIdAndEventId(eventId: number, slotId: number): Observable<SlotResponseDto> {
    return this.httpClient.get<SlotResponseDto>(`${this.eventsUrl}/${eventId}/slots/${slotId}`).pipe(
      tap(() =>
        this.logger.debug(`[SlotService] GET slots by slotId and eventId successful`)
      ),
      catchError(error => {
        this.logger.warn(`[SlotService] GET slots by slotId and eventId failed`);
        return throwError(() => error);
      })
    );
  }

  putSlotByIdAndEventId(eventId: number, slotId: number, slotRequestDto: SlotRequestDto): Observable<SlotResponseDto> {
    return this.httpClient.put<SlotResponseDto>(`${this.eventsUrl}/${eventId}/slots/${slotId}`, slotRequestDto).pipe(
      tap(() => this.logger.info(`[SlotService] PUT slot successful`)),
      catchError((error) => {
        this.logger.warn(`[SlotService] PUT slot failed`);
        return throwError(() => error);
      }),
    );
  }

  slotDeleteValidation(eventId: number, slotId: number): Observable<DeleteValidationResponseDto> {
    return this.httpClient.get<DeleteValidationResponseDto>(`${this.eventsUrl}/${eventId}/slots/${slotId}/delete-validation`).pipe(
      tap(() => this.logger.debug(`[SlotService] Get delete validation response successful`)),
      catchError((error) => {
        this.logger.warn(`[SlotService] Get delete validation response failed`);
        return throwError(() => error);
      }),
    );
  }
}