import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { LogLevel } from '@core/model/log-level';

@Injectable({
  providedIn: 'root',
})
export class LoggerService {
  private readonly levels: Record<LogLevel, number> = {
    debug: 10,
    info: 20,
    warn: 30,
    error: 40,
    off: 99,
  };

  private readonly currentLevel: LogLevel = environment.logLevel;

  debug(message:string, ...data: unknown[]):void {
    if(this.shouldLog(LogLevel.Debug)){
      console.debug(message, ...data);
    }
  }

  info(message:string, ...data: unknown[]):void {
    if(this.shouldLog(LogLevel.Info)){
      console.info(message, ...data);
    }
  }

  warn(message: string, ...data: unknown[]): void {
    if (this.shouldLog(LogLevel.Warn)) {
      console.warn(message, ...data);
    }
  }

  error(message: string, ...data: unknown[]): void {
    if (this.shouldLog(LogLevel.Error)) {
      console.error(message, ...data);
    }
  }

  private shouldLog(loglevel: LogLevel):boolean {
    return this.levels[loglevel] >= this.levels[this.currentLevel]; 
  }
}
