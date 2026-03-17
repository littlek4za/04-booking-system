import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, catchError, filter, Observable, of, switchMap, tap } from 'rxjs';
import { SlotResponseDto } from './dtos/slot-response-dto';
import { SlotRequestDto } from './dtos/slot-request-dto';

@Injectable({
  providedIn: 'root',
})
export class SlotService {

  private eventId$ = new BehaviorSubject<number | null>(null);
  slotList$ = this.eventId$.pipe(
    filter((id): id is number => id !== null), //skip null
    switchMap(id => this.getSlotsByEventId(id).pipe(
      tap((res) => console.log('GET Slot List succeed', res)),
      catchError(err => {
        console.log('GET Slotlist by event failed');
        return of([]);
      }))
    ));

  private eventsUrl = "http://localhost:8080/api/v1/events"

  constructor(private httpClient: HttpClient) { }

  getSlotsByEventId(eventId: number): Observable<SlotResponseDto[]> {
    return this.httpClient.get<SlotResponseDto[]>(`${this.eventsUrl}/${eventId}/slots`);
  }

  triggerRefresh(eventId: number) {
    this.eventId$.next(eventId);
  }

  createSlotByEventId(slotRequestDto: SlotRequestDto, eventId: number): Observable<SlotResponseDto> {
    return this.httpClient.post<SlotResponseDto>(`${this.eventsUrl}/${eventId}/slots`, slotRequestDto);
  }

  deleteSlotByIdAndEvent(eventId: number, slotId: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.eventsUrl}/${eventId}/slots/${slotId}`);
  }

  getSlotByIdAndEventId(eventId: number, slotId: number): Observable<SlotResponseDto> {
    return this.httpClient.get<SlotResponseDto>(`${this.eventsUrl}/${eventId}/slots/${slotId}`)
  }

  putSlotByIdAndEventId(eventId:number, slotId: number, slotRequestDto: SlotRequestDto): Observable<SlotResponseDto> {
    return this.httpClient.put<SlotResponseDto>(`${this.eventsUrl}/${eventId}/slots/${slotId}`, slotRequestDto);
  }
}