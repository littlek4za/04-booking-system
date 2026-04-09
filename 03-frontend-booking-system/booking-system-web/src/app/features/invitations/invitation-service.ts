import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { InvitationResponseDto } from './dtos/invitation-response-dto';
import { InvitationRequestDto } from './dtos/invitation-request-dto';
import { BehaviorSubject, catchError, filter, Observable, of, switchMap, tap } from 'rxjs';
import { InvitationValidationResponseDto } from './dtos/invitation-validation-response-dto';

@Injectable({
  providedIn: 'root',
})
export class InvitationService {

  private eventId$ = new BehaviorSubject<number | null>(null);
  invitationListByEventId$ = this.eventId$.pipe(
    filter((id): id is number => id !==null),
    switchMap(id => this.getInvitationsByEventId(id).pipe(
      tap((res) => console.log('GET Invitation list succeed', res)),
      catchError( err => {
        console.log('GET inivitation list by event failed');
        return of([]);
      })
    ))
  );

  private token$ = new BehaviorSubject<string | null> (null);
  invitationByToken$ = this.token$.pipe(
    filter((token): token is string => token !== null),
    switchMap(token => this.getInvitationByToken(token).pipe(
      tap((res)=> console.log('GET Invitation succeed', res)),
      catchError( err => {
        console.log('GET inivitation by token failed');
        return of(null);
      })
    ))
  );

  private eventsUrl = "http://localhost:8080/api/v1/events";
  private invitationUrl = "http://localhost:8080/api/v1/invitations";

  constructor(private httpClient: HttpClient) {
  }

  createInvitation(invitationRequestDto: InvitationRequestDto, eventId: number): Observable<InvitationResponseDto> {
    const url = `${this.eventsUrl}/${eventId}/invitations`;
    return this.httpClient.post<InvitationResponseDto>(url, invitationRequestDto);
  }

  getInvitationsByEventId(eventId: number): Observable<InvitationResponseDto[]> {
    const url = `${this.eventsUrl}/${eventId}/invitations`;
    return this.httpClient.get<InvitationResponseDto[]>(url);
  }

  getInvitationsByEventIdAndSlotId(eventId:number, slotId: number): Observable<InvitationResponseDto[]> {
    const url = `${this.eventsUrl}/${eventId}/invitations?slotId=${slotId}`;
    return this.httpClient.get<InvitationResponseDto[]>(url);
  }

  getInvitationByToken(token: string): Observable< InvitationResponseDto> {
    const url = `${this.invitationUrl}/${token}`;
    return this.httpClient.get<InvitationResponseDto>(url);
  }

  deleteInvitation(eventId: number, invitationId: number): Observable<void> {
    const url = `${this.eventsUrl}/${eventId}/invitations/${invitationId}`;
    return this.httpClient.delete<void>(url);
  }

  validateInvitation(token: string): Observable<InvitationValidationResponseDto> {
    const url = `${this.invitationUrl}/${token}/validate`;
    return this.httpClient.get<InvitationValidationResponseDto>(url);
  }

  triggerRefreshForInvitationList(eventId: number){
    this.eventId$.next(eventId);
  }

  triggerRefreshForInvitation(token: string){
    this.token$.next(token);
  }

}
