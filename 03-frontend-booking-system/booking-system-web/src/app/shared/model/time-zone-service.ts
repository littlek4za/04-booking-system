import { Injectable } from '@angular/core';
import moment from 'moment-timezone';
import { TimeZoneOption } from './time-zone-option';

@Injectable({
  providedIn: 'root',
})
export class TimeZoneService {
  
  getUserTimeZone(): string {
    return Intl.DateTimeFormat().resolvedOptions().timeZone;
  }

  getAllTimeZones(): TimeZoneOption[] {
    const defaultTimeZoneList = moment.tz.names();

    return defaultTimeZoneList.map(tz =>{
      const offset = moment.tz(tz).utcOffset();
      const sign = offset >= 0 ? '+' : '-';
      const absOffset = Math.abs(offset);

      const hours = Math.floor(absOffset/60);
      const minutes = absOffset % 60;

      const formattedOffset = 
      `GMT${sign}${hours.toString().padStart(2,'0')}:${minutes.toString().padStart(2,'0')}`;

      return {
        value: tz,
        label: `(${formattedOffset}) ${tz}`,
        offset: offset
      }
    }).sort((a,b) => a.offset - b.offset);
  }
}
