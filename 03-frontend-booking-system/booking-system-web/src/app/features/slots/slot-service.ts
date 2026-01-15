import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, catchError, filter, of, switchMap, tap } from 'rxjs';
import { SlotResponseDto } from './dtos/slot-response-dto';

@Injectable({
  providedIn: 'root',
})
export class SlotService {

  private eventId$ = new BehaviorSubject<number | null>(null);
  slot$ = this.eventId$.pipe(
    filter((id): id is number => id !== null), //skip null
    switchMap(id => this.getSlotsByEvent(id).pipe(
      tap((res)=>console.log('GET Slot List succeed', res)),
      catchError(err => {
        console.log('GET Slotlist by event failed');
        return of([]);
      }))
    ));

  private eventsUrl = "http://localhost:8080/api/v1/events"

  constructor(private httpClient: HttpClient) { }

  getSlotsByEvent(eventId: number) {
    return this.httpClient.get<SlotResponseDto[]>(`${this.eventsUrl}/${eventId}/slots`)
  }

  triggerRefresh(eventId: number) {
    this.eventId$.next(eventId);
  }
}
