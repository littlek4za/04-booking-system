import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { InvitationResponseDto } from './dtos/invitation-response-dto';
import { InvitationRequestDto } from './dtos/invitation-request-dto';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class InvitationService {

  private eventsUrl = "http://localhost:8080/api/v1/events";

  constructor(private httpClient: HttpClient) {

  }

  createInvitation(invitationRequestDto: InvitationRequestDto, eventId: number): Observable<InvitationResponseDto> {
    const invitationUrl = `${this.eventsUrl}/${eventId}/invitations`
    return this.httpClient.post<InvitationResponseDto>(invitationUrl, invitationRequestDto);
  }

}
