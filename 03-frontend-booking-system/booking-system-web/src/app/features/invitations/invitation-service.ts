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
  invitation$ = this.eventId$.pipe(
    filter((id): id is number => id !==null),
    switchMap(id => this.getInvitationsByEventId(id).pipe(
      tap((res) => console.log('GET Invitation list succeed', res)),
      catchError( err => {
        console.log('GET Inivitation list by event failed');
        return of([]);
      })
    ))
  )

  private eventsUrl = "http://localhost:8080/api/v1/events";
  private validateInvitationUrl = "http://localhost:8080/api/v1/invitations";

  constructor(private httpClient: HttpClient) {
  }

  createInvitation(invitationRequestDto: InvitationRequestDto, eventId: number): Observable<InvitationResponseDto> {
    const invitationUrl = `${this.eventsUrl}/${eventId}/invitations`;
    return this.httpClient.post<InvitationResponseDto>(invitationUrl, invitationRequestDto);
  }

  getInvitationsByEventId(eventId: number): Observable<InvitationResponseDto[]> {
    const invitationUrl = `${this.eventsUrl}/${eventId}/invitations`;
    return this.httpClient.get<InvitationResponseDto[]>(invitationUrl);
  }

  deleteInvitation(eventId: number, invitationId: number): Observable<void> {
    const invitationUrl = `${this.eventsUrl}/${eventId}/invitations/${invitationId}`;
    return this.httpClient.delete<void>(invitationUrl);
  }

  validateInvitation(token: string): Observable<InvitationValidationResponseDto> {
    const url = `${this.validateInvitationUrl}/${token}/validate`;
    return this.httpClient.get<InvitationValidationResponseDto>(url);
  }

  triggerRefresh(eventId: number){
    this.eventId$.next(eventId);
  }

}
