import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { AuthService } from "../../features/auth/auth-service";
import { catchError, throwError } from "rxjs";
import { ErrorMapperService } from "@core/services/error-mapper-service";
import { LoggerService } from "@core/services/logger-service";
import { NotificationService } from "@core/services/notification-service";

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const errorMapperService = inject(ErrorMapperService);
    const notificationService = inject(NotificationService);
    const logger = inject(LoggerService);

    return next(req).pipe(
        catchError((err: HttpErrorResponse) => {

            const appError = errorMapperService.mapError(err);

            logger.error(`[API Error] ${req.method} ${req.url}:`, appError);

            if (err.status === 401 && req.headers.get('Authorization')) {
                authService.logoutByExpiry();
                return throwError(() => err);
            }

            notificationService.error(errorMapperService.toMessage(appError, err.status));
            return throwError(() => err);
        })
    );
}