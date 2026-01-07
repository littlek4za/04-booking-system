import { Injectable } from '@angular/core';
import { EventSaveRequestDto } from '../common/event-save-request-dto';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { EventResponseDto } from '../common/event-response-dto';

@Injectable({
  providedIn: 'root',
})
export class EventService {

  private saveEventUrl = "http://localhost:8080/api/v1/events";

  constructor(private httpClient: HttpClient, private router:Router){}

  saveEvent(eventSaveRequestDto: EventSaveRequestDto): Observable<EventResponseDto> {
    return this.httpClient.post<EventResponseDto>(this.saveEventUrl, eventSaveRequestDto);
  }
  
}
