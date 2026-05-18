import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { InvitationResponseDto } from './dtos/invitation-response-dto';
import { InvitationRequestDto } from './dtos/invitation-request-dto';
import { BehaviorSubject, catchError, filter, Observable, of, switchMap, tap, throwError } from 'rxjs';
import { InvitationValidationResponseDto } from './dtos/invitation-validation-response-dto';
import { LoggerService } from '@core/services/logger-service';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class InvitationService {

  private logger = inject(LoggerService);

  private eventId$ = new BehaviorSubject<number | null>(null);
  invitationListByEventId$ = this.eventId$.pipe(
    filter((id): id is number => id !== null),
    switchMap(id => this.getInvitationsByEventId(id).pipe(
      catchError( () => {
        return of([]);
      })
    )),
  );

  private token$ = new BehaviorSubject<string | null>(null);
  invitationByToken$ = this.token$.pipe(
    filter((token): token is string => token !== null),
    switchMap(token => this.getInvitationByToken(token).pipe(
      catchError( () => {
        return of(null);
      })
    )),
  );

  private eventsUrl = `${environment.backendApiUrl}/v1/events`;
  private invitationUrl = `${environment.backendApiUrl}/v1/invitations`;

  constructor(private httpClient: HttpClient) {
  }

  createInvitation(invitationRequestDto: InvitationRequestDto, eventId: number): Observable<InvitationResponseDto> {
    const url = `${this.eventsUrl}/${eventId}/invitations`;
    return this.httpClient.post<InvitationResponseDto>(url, invitationRequestDto).pipe(
      tap(() =>
        this.logger.info(`[InvitationService] CREATE invitation successful:`)
      ),
      catchError(error => {
        this.logger.warn(`[InvitationService] CREATE invitation failed`);
        return throwError(() => error);
      })
    );
  }

  getInvitationsByEventId(eventId: number): Observable<InvitationResponseDto[]> {
    const url = `${this.eventsUrl}/${eventId}/invitations`;
    return this.httpClient.get<InvitationResponseDto[]>(url).pipe(
      tap(() =>
        this.logger.debug(`[InvitationService] GET invitation by event id successful:`)
      ),
      catchError(error => {
        this.logger.warn(`[InvitationService] GET invitation by event id failed`);
        return throwError(() => error);
      })
    );
  }

  getInvitationsByEventIdAndSlotId(eventId: number, slotId: number): Observable<InvitationResponseDto[]> {
    const url = `${this.eventsUrl}/${eventId}/invitations?slotId=${slotId}`;
    return this.httpClient.get<InvitationResponseDto[]>(url).pipe(
      tap(() =>
        this.logger.debug(`[InvitationService] GET invitations by event id and slot id successful`)
      ),
      catchError(error => {
        this.logger.warn(`[InvitationService] GET invitations by event id and slot id successful failed`);
        return throwError(() => error);
      })
    );
  }

  getInvitationByToken(token: string): Observable<InvitationResponseDto> {
    const url = `${this.invitationUrl}/by-token/${token}`;
    return this.httpClient.get<InvitationResponseDto>(url).pipe(
      tap(() =>
        this.logger.debug(`[InvitationService] GET invitation by token successful`)
      ),
      catchError(error => {
        this.logger.warn(`[InvitationService] GET invitation by token failed`);
        return throwError(() => error);
      })
    );
  }

  deleteInvitation(eventId: number, invitationId: number): Observable<void> {
    const url = `${this.eventsUrl}/${eventId}/invitations/${invitationId}`;
    return this.httpClient.delete<void>(url).pipe(
      tap(() =>
        this.logger.info(`[InvitationService] DELETE invitation by eventId and invitationId successful`)
      ),
      catchError(error => {
        this.logger.warn(`[InvitationService] DELETE invitation by eventId and invitationId failed`);
        return throwError(() => error);
      })
    );
  }

  validateInvitation(token: string): Observable<InvitationValidationResponseDto> {
    const url = `${this.invitationUrl}/by-token/${token}/validate`;
    return this.httpClient.get<InvitationValidationResponseDto>(url).pipe(
      tap(() => {
        this.logger.debug('[InvitationService] Get invitation validation response succesful', token);
      }),
      catchError(error => {
        this.logger.warn('[InvitationService] Get invitation validation response failed');
        return throwError(() => error);
      })
    );
  }

  triggerRefreshForInvitationListByEventId(eventId: number) {
    this.eventId$.next(eventId);
  }

  triggerRefreshForInvitationByToken(token: string) {
    this.token$.next(token);
  }

}
